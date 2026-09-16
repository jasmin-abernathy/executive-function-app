package org.lepotager.executivefunction.domain

import org.junit.Assert.*
import org.junit.Test
import org.lepotager.executivefunction.model.FocusStatus

class FocusMiniActionsTest {
    @Test
    fun runningOffersPauseAndQuickNote() {
        assertEquals(
            listOf(FocusMiniCommand.PAUSE, FocusMiniCommand.ADD_NOTE),
            FocusMiniActions.commands(FocusStatus.RUNNING),
        )
        assertEquals("pause", FocusMiniActions.receiverAction(FocusMiniCommand.PAUSE))
    }

    @Test
    fun interruptedOffersResumeAndQuickNote() {
        assertEquals(
            listOf(FocusMiniCommand.RESUME, FocusMiniCommand.ADD_NOTE),
            FocusMiniActions.commands(FocusStatus.INTERRUPTED),
        )
        assertEquals("resume", FocusMiniActions.receiverAction(FocusMiniCommand.RESUME))
    }

    @Test
    fun finishedStatesExposeNoMiniActions() {
        assertTrue(FocusMiniActions.commands(FocusStatus.POSTPONED).isEmpty())
        assertTrue(FocusMiniActions.commands(FocusStatus.COMPLETED).isEmpty())
        assertNull(FocusMiniActions.receiverAction(FocusMiniCommand.ADD_NOTE))
    }
}
