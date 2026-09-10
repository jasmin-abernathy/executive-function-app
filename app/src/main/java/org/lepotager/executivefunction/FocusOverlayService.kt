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
import kotlin.math.abs
import kotlin.math.roundToInt

class FocusOverlayService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var database: AppDatabase
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var timerView: TextView? = null

    private val ticker = object : Runnable {
        override fun run() {
            val active = database.activeFocus()
            if (active?.session?.status != FocusStatus.RUNNING) {
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
        if (!Settings.canDrawOverlays(this) || active?.session?.status != FocusStatus.RUNNING) {
            setEnabledPreference(false)
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(NOTIFICATION_ID, buildNotification(active))
        if (overlayView == null) createOverlay()
        handler.removeCallbacks(ticker)
        ticker.run()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        overlayView?.let { runCatching { windowManager.removeView(it) } }
        overlayView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createOverlay() {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(9), dp(8), dp(9))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(22).toFloat()
                setColor(Color.rgb(232, 244, 235))
                setStroke(dp(1), Color.rgb(174, 205, 182))
            }
            elevation = dp(8).toFloat()
        }
        timerView = TextView(this).apply {
            setTextColor(Color.rgb(28, 49, 35))
            textSize = 18f
            typeface = Typeface.MONOSPACE
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(10), 0, dp(8), 0)
        }
        val close = TextView(this).apply {
            text = "×"
            textSize = 22f
            gravity = Gravity.CENTER
            contentDescription = getString(R.string.hide_floating_timer)
            setTextColor(Color.rgb(54, 70, 59))
            setPadding(dp(8), dp(2), dp(8), dp(2))
            setOnClickListener {
                setEnabledPreference(false)
                stopSelf()
            }
        }
        row.addView(timerView)
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
        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false
        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
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
                    params.x = (startX + dx).coerceAtLeast(0)
                    params.y = (startY + dy).coerceAtLeast(0)
                    windowManager.updateViewLayout(view, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) openApp()
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
        timerView?.text = prefix + formatTime(value)
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

    private fun formatTime(milliseconds: Long): String {
        val seconds = milliseconds / 1_000
        val hours = seconds / 3_600
        val minutes = (seconds % 3_600) / 60
        val remainder = seconds % 60
        return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, remainder)
        else "%02d:%02d".format(minutes, remainder)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

    companion object {
        const val PREFERENCES = "focus_overlay"
        const val KEY_ENABLED = "enabled"
        private const val CHANNEL_ID = "active_focus"
        private const val NOTIFICATION_ID = 3107

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, FocusOverlayService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FocusOverlayService::class.java))
        }
    }
}
