package org.lepotager.executivefunction

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Context
import android.graphics.drawable.Icon
import android.util.Rational
import org.lepotager.executivefunction.domain.FocusMiniActions
import org.lepotager.executivefunction.domain.FocusMiniCommand
import org.lepotager.executivefunction.model.ActiveFocus

/**
 * Android-specific rendering of the pure reduced-focus commands.
 * MainActivity owns the lifecycle and supplies the quick-note PendingIntent.
 */
internal object PipFocusSurface {
    fun params(
        context: Context,
        active: ActiveFocus,
        addNoteIntent: PendingIntent,
    ): PictureInPictureParams {
        val actions = FocusMiniActions.commands(active.session.status).mapNotNull { command ->
            when (command) {
                FocusMiniCommand.PAUSE -> remote(
                    context,
                    R.drawable.ic_pip_pause,
                    R.string.pip_pause,
                    FocusPresence.action(context, active, requireNotNull(FocusMiniActions.receiverAction(command))),
                )
                FocusMiniCommand.RESUME -> remote(
                    context,
                    R.drawable.ic_pip_resume,
                    R.string.pip_resume,
                    FocusPresence.action(context, active, requireNotNull(FocusMiniActions.receiverAction(command))),
                )
                FocusMiniCommand.ADD_NOTE -> remote(
                    context,
                    R.drawable.ic_pip_note_add,
                    R.string.pip_add_note,
                    addNoteIntent,
                )
            }
        }
        return PictureInPictureParams.Builder()
            .setAspectRatio(Rational(4, 3))
            .setActions(actions)
            .build()
    }

    private fun remote(
        context: Context,
        icon: Int,
        label: Int,
        intent: PendingIntent,
    ): RemoteAction {
        val text = context.getString(label)
        return RemoteAction(Icon.createWithResource(context, icon), text, text, intent)
    }
}
