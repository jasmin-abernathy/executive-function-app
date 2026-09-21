package org.lepotager.executivefunction

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.lepotager.executivefunction.data.AppDatabase
import org.lepotager.executivefunction.domain.SessionClock
import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.model.FocusStatus
import org.lepotager.executivefunction.ui.FocusClockText
import kotlin.math.abs
import kotlin.math.roundToInt

class FocusOverlayService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var database: AppDatabase
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var timerView: TextView? = null
    private var pauseView: TextView? = null
    private val dimOverlay = Runnable {
        overlayView?.animate()?.alpha(IDLE_ALPHA)?.setDuration(180L)?.start()
    }

    private val ticker = object : Runnable {
        override fun run() {
            val active = database.activeFocus()
            if (active == null || active.session.status !in setOf(FocusStatus.RUNNING, FocusStatus.INTERRUPTED)) {
                setEnabledPreference(false)
                stopSelf()
                return
            }
            render(active)
            handler.postDelayed(this, 1_000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase(applicationContext)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val active = database.activeFocus()
        if (active == null ||
            active.session.status !in setOf(FocusStatus.RUNNING, FocusStatus.INTERRUPTED)
        ) {
            removeOverlay()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        // The foreground notification is the persistent focus presence, including lock screen.
        // The floating overlay is optional and must not own the timer lifecycle.
        startForeground(FocusPresence.ID, FocusPresence.build(this, active))

        val shouldShowOverlay =
            getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false) &&
                Settings.canDrawOverlays(this)
        handler.removeCallbacks(ticker)
        if (shouldShowOverlay) {
            if (overlayView == null) createOverlay()
            ticker.run()
        } else {
            removeOverlay()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        handler.removeCallbacks(dimOverlay)
        removeOverlay()
        // Detach keeps the SystemUI chronometer from visually jumping if Android restarts
        // this sticky service after killing the process. Explicit session end cancels it.
        stopForeground(STOP_FOREGROUND_DETACH)
        database.close()
        super.onDestroy()
    }

    private fun removeOverlay() {
        handler.removeCallbacks(dimOverlay)
        overlayView?.let { runCatching { windowManager.removeView(it) } }
        overlayView = null
        timerView = null
        pauseView = null
    }

    private fun wakeOverlay() {
        handler.removeCallbacks(dimOverlay)
        overlayView?.animate()?.cancel()
        overlayView?.alpha = 1f
        handler.postDelayed(dimOverlay, OPAQUE_AFTER_TOUCH_MS)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createOverlay() {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            alpha = IDLE_ALPHA
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(9), dp(8), dp(9))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(22).toFloat()
                setColor(getColor(R.color.floating_background))
                setStroke(dp(1), getColor(R.color.floating_border))
            }
            elevation = dp(8).toFloat()
        }
        timerView = TextView(this).apply {
            setTextColor(getColor(R.color.floating_text))
            textSize = 18f
            typeface = Typeface.MONOSPACE
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(10), 0, dp(8), 0)
        }
        pauseView = TextView(this).apply {
            text = getString(R.string.overlay_pause_symbol)
            textSize = 20f
            gravity = Gravity.CENTER
            contentDescription = getString(R.string.interrupt_action)
            setTextColor(getColor(R.color.floating_text))
            minimumWidth = dp(48)
            minimumHeight = dp(48)
            setPadding(dp(6), dp(2), dp(6), dp(2))
            setOnClickListener { wakeOverlay(); togglePauseResume() }
        }
        val note = TextView(this).apply {
            text = getString(R.string.overlay_note_symbol)
            textSize = 24f
            gravity = Gravity.CENTER
            contentDescription = getString(R.string.pip_add_note)
            setTextColor(getColor(R.color.floating_text))
            minimumWidth = dp(48)
            minimumHeight = dp(48)
            setPadding(dp(6), dp(2), dp(6), dp(2))
            setOnClickListener { wakeOverlay(); openQuickNote() }
        }
        val close = TextView(this).apply {
            text = "×"
            textSize = 22f
            gravity = Gravity.CENTER
            contentDescription = getString(R.string.hide_floating_timer)
            setTextColor(getColor(R.color.floating_text))
            minimumWidth = dp(48)
            minimumHeight = dp(48)
            setPadding(dp(8), dp(2), dp(8), dp(2))
            setOnClickListener {
                wakeOverlay()
                setEnabledPreference(false)
                // Keep the foreground timer service alive; only remove the floating view.
                removeOverlay()
            }
        }
        row.addView(timerView)
        row.addView(pauseView)
        row.addView(note)
        row.addView(close)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = dp(12)
            y = dp(96)
        }
        installDragAndOpen(row, params)
        windowManager.addView(row, params)
        overlayView = row
    }

    private fun installDragAndOpen(view: View, params: WindowManager.LayoutParams) {
        view.setOnClickListener { wakeOverlay(); openApp() }
        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false
        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    wakeOverlay()
                    startX = params.x
                    startY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (touchX - event.rawX).roundToInt()
                    val dy = (event.rawY - touchY).roundToInt()
                    moved = moved || abs(dx) > dp(4) || abs(dy) > dp(4)
                    params.x = (startX + dx).coerceIn(0, (resources.displayMetrics.widthPixels - view.width).coerceAtLeast(0))
                    params.y = (startY + dy).coerceIn(0, (resources.displayMetrics.heightPixels - view.height).coerceAtLeast(0))
                    windowManager.updateViewLayout(view, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) view.performClick()
                    true
                }
                else -> false
            }
        }
    }

    private fun render(active: ActiveFocus) {
        val elapsed = SessionClock.elapsedMs(active.session, System.currentTimeMillis())
        val target = active.session.targetDurationMs
        val value = if (target == null) elapsed else abs(target - elapsed)
        val prefix = if (target != null && elapsed > target) "+" else ""
        timerView?.text = prefix + FocusClockText.format(value)
        pauseView?.apply {
            text = getString(
                if (active.session.status == FocusStatus.RUNNING) R.string.overlay_pause_symbol
                else R.string.overlay_resume_symbol,
            )
            contentDescription = getString(
                if (active.session.status == FocusStatus.RUNNING) R.string.interrupt_action
                else R.string.resume_action,
            )
        }
    }

    private fun togglePauseResume() {
        val active = database.activeFocus() ?: return
        val action = when (active.session.status) {
            FocusStatus.RUNNING -> "pause"
            FocusStatus.INTERRUPTED -> "resume"
            else -> return
        }
        sendBroadcast(
            Intent(this, FocusActionReceiver::class.java)
                .setAction(action)
                .putExtra("session", active.session.id),
        )
        handler.postDelayed({ database.activeFocus()?.let(::render) }, 120L)
    }

    private fun openQuickNote() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("quick_action", "overlay_note")
            },
        )
    }

    private fun buildNotification(active: ActiveFocus): android.app.Notification {
        val elapsed = SessionClock.elapsedMs(active.session, System.currentTimeMillis())
        val target = active.session.targetDurationMs
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentTitle(getString(R.string.focus_notification_channel))
            .setContentText(getString(R.string.floating_timer_running))
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setWhen(
                if (target == null) System.currentTimeMillis() - elapsed
                else System.currentTimeMillis() + (target - elapsed).coerceAtLeast(0),
            )
            .setUsesChronometer(true)
            .setChronometerCountDown(target != null && elapsed < target)
            .build()
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.focus_notification_channel),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = getString(R.string.focus_notification_channel_description)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(null, null)
                enableVibration(false)
            },
        )
    }

    private fun openApp() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
        )
    }

    private fun setEnabledPreference(enabled: Boolean) {
        getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

    companion object {
        const val PREFERENCES = "focus_overlay"
        const val KEY_ENABLED = "enabled"
        private const val CHANNEL_ID = FocusPresence.CHANNEL
        private const val NOTIFICATION_ID = 3107
        private const val IDLE_ALPHA = 0.5f
        private const val OPAQUE_AFTER_TOUCH_MS = 2_500L

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, FocusOverlayService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FocusOverlayService::class.java))
        }
    }
}
