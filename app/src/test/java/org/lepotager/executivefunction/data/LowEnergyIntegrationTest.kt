package org.lepotager.executivefunction.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LowEnergyIntegrationTest {
    @Test fun latestMoodChangesSuggestionsWithoutChangingTheRunningTimer() = runBlocking {
        val context: Context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("executive-function.db")
        context.getSharedPreferences("wellbeing", 0).edit().clear().commit()
        val db = AppDatabase(context)
        try {
            var time = System.currentTimeMillis() - 1_000L
            val repository = FocusRepository(db, Dispatchers.Unconfined)
            val gentle = repository.capture("Petite action")
            val demanding = repository.capture("Gros effort")
            LearningJournal(db).plan(gentle, Planning(energy = 1))
            LearningJournal(db).plan(demanding, Planning(energy = 3))
            repository.start(demanding, 1_500_000L)
            val session = db.activeFocus()!!.session
            repository.recordCheckIn(1, 3, 3)
            assertNotNull(repository.snapshot.value.lowEnergyUntil)
            assertEquals(setOf(gentle), repository.snapshot.value.eligibleDrawIds)
            assertEquals(session, db.activeFocus()!!.session)
            // Different timestamps make the most recent check-in deterministic.
            db.writableDatabase.execSQL("UPDATE check_ins SET recorded_at=?", arrayOf(time))
            repository.recordCheckIn(3, 3, 3)
            assertNull(repository.snapshot.value.lowEnergyUntil)
            assertNull(repository.snapshot.value.eligibleDrawIds)
            assertEquals(session, db.activeFocus()!!.session)
        } finally { db.close() }
    }
}
