package org.lepotager.executivefunction.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RoutineStoreTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()
    private fun fresh(): AppDatabase {
        context.deleteDatabase("executive-function.db")
        return AppDatabase(context)
    }

    @Test fun sequencePausesAndResumesWithoutDiscardingPastSteps() {
        val db = fresh()
        try {
            val store = RoutineStore(db)
            val id = store.create("Matin")
            store.addStep(id, "Petit-déjeuner", null)
            store.addStep(id, "Douche", null)
            store.addStep(id, "Partir", 8 * 60 + 30)
            val run = store.startOrResume(id)
            store.decide(run, 0, false)
            store.pause(run)
            assertEquals("PAUSED", store.load().single().openRun!!.status)
            assertEquals(run, store.startOrResume(id))
            val resumed = store.load().single().openRun!!
            assertEquals(listOf("DONE","PENDING","PENDING"), resumed.steps.map { it.state })
            store.decide(run, 1, true)
            store.decide(run, 2, false)
            assertNull(store.load().single().openRun)
            assertNotEquals(run, store.startOrResume(id))
            assertTrue(store.load().single().openRun!!.steps.all { it.state == "PENDING" })
        } finally { db.close() }
    }

    @Test fun editingTemplateCannotAlterAnExistingRun() {
        val db = fresh()
        try {
            val store = RoutineStore(db)
            val id = store.create("Départ")
            val first = store.addStep(id,"Préparer les affaires",null)
            val second = store.addStep(id,"Partir",510)
            val run = store.startOrResume(id)
            store.editStep(id,first,"Préparer le sac",null)
            store.deleteStep(id,second)
            assertEquals(listOf("Préparer le sac"),store.load().single().steps.map { it.title })
            assertEquals(listOf("Préparer les affaires","Partir"),store.load().single().openRun!!.steps.map { it.title })
            assertEquals(510,store.load().single().openRun!!.steps[1].clockMinute)
            assertEquals(run,store.startOrResume(id))
        } finally { db.close() }
    }

    @Test fun reorderAndOptionalClockBehavePredictably() {
        val db = fresh()
        try {
            val store = RoutineStore(db)
            val id = store.create("Ma journée")
            val first = store.addStep(id,"D'abord",null)
            store.addStep(id,"Ensuite",510)
            store.moveStep(id,first,+1)
            assertEquals(listOf("Ensuite","D'abord"),store.load().single().steps.map { it.title })
            assertNull(RoutineClock.parse(""))
            assertEquals(0,RoutineClock.parse("00:00"))
            assertEquals(1439,RoutineClock.parse("23:59"))
            assertEquals("08:30",RoutineClock.format(510))
            assertThrows(IllegalArgumentException::class.java) { RoutineClock.parse("24:00") }
            assertThrows(IllegalArgumentException::class.java) { RoutineClock.parse("8:30") }
        } finally { db.close() }
    }

    @Test fun versionSixUpgradeKeepsTasksAndCreatesRoutineTables() = runBlocking {
        context.deleteDatabase("executive-function.db")
        var db = AppDatabase(context)
        try {
            val id = FocusRepository(db, Dispatchers.Unconfined).capture("Ancienne tâche")
            RoutineStore.tables.reversed().forEach { db.writableDatabase.execSQL("DROP TABLE " + it) }
            db.writableDatabase.version = 6
            db.close()
            db = AppDatabase(context)
            assertEquals(7,db.writableDatabase.version)
            assertEquals(id,db.openTasks().single().id)
            assertTrue(RoutineStore(db).load().isEmpty())
        } finally { db.close() }
    }

    @Test fun backupRoundTripAndOlderBackupImportAreAtomic() {
        val db = fresh()
        try {
            val store = RoutineStore(db)
            val id = store.create("Routine à garder")
            store.addStep(id,"Étape",510)
            val run = store.startOrResume(id)
            store.decide(run,0,false)
            val backup = LocalBackup(db).export()
            assertEquals(7,JSONObject(backup).getInt("version"))
            LocalBackup(db).clear()
            assertTrue(store.load().isEmpty())
            LocalBackup(db).restore(backup)
            assertEquals(listOf("Étape"),store.load().single().steps.map { it.title })
            val invalid = JSONObject(backup)
            invalid.getJSONArray("routine_steps").getJSONObject(0).put("routine_id","missing-id")
            assertThrows(Exception::class.java) { LocalBackup(db).restore(invalid.toString()) }
            assertEquals("Routine à garder",store.load().single().title)
            val legacy = JSONObject(backup).put("version",6)
            RoutineStore.tables.forEach { legacy.remove(it) }
            LocalBackup(db).restore(legacy.toString())
            assertTrue(store.load().isEmpty())
        } finally { db.close() }
    }

    @Test fun unfinishedOrWrongStepsNeverCompleteOutOfOrder() {
        val db = fresh()
        try {
            val store = RoutineStore(db)
            val id = store.create("R")
            assertThrows(IllegalArgumentException::class.java) { store.startOrResume(id) }
            store.addStep(id,"Un",null)
            store.addStep(id,"Deux",null)
            val run = store.startOrResume(id)
            assertThrows(IllegalArgumentException::class.java) { store.decide(run,1,false) }
            store.decide(run,0,false)
            assertThrows(IllegalArgumentException::class.java) { store.decide(run,0,true) }
            assertEquals("PENDING",store.load().single().openRun!!.steps.last().state)
        } finally { db.close() }
    }
}
