package org.lepotager.executivefunction.data

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RegulationCardStoreTest {
    private fun database(): AppDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("executive-function.db")
        return AppDatabase(context)
    }

    @Test fun trimsAndRejectsInvalidCards() {
        assertEquals(
            "Un pas" to listOf("Boire de l'eau", "Se poser"),
            RegulationCardRules.normalize("  Un pas  ", listOf(" Boire de l'eau ", "", " Se poser ")),
        )
        assertThrows(IllegalArgumentException::class.java) { RegulationCardRules.normalize("", listOf("Un pas")) }
        assertThrows(IllegalArgumentException::class.java) { RegulationCardRules.normalize("Sans étapes", emptyList()) }
        assertThrows(IllegalArgumentException::class.java) { RegulationCardRules.normalize("Quatre", listOf("1", "2", "3", "4")) }
        assertThrows(IllegalArgumentException::class.java) { RegulationCardRules.normalize("Trop long", listOf("x".repeat(161))) }
    }

    @Test fun createsEditsAndDeletesCardsOffline() {
        val db = database()
        try {
            val cards = RegulationCardStore(db)
            val created = cards.save(null, " Respirer ", listOf(" S'asseoir ", "Réduire le bruit"))
            assertEquals(listOf(created), cards.list())
            val edited = cards.save(created.id, "Faire une pause", listOf("Prendre un verre d'eau"))
            assertEquals(listOf(edited), cards.list())
            assertThrows(IllegalArgumentException::class.java) {
                cards.save("non-existent", "Erreur", listOf("Un pas"))
            }
            cards.delete(created.id)
            assertTrue(cards.list().isEmpty())
        } finally { db.close() }
    }

    @Test fun regularBackupPreservesCardsAndOlderBackupsRestoreWithoutThem() {
        val db = database()
        try {
            val cards = RegulationCardStore(db)
            val created = cards.save(null, "Pause", listOf("Aller dans une pièce calme"))
            val backup = LocalBackup(db)
            val exported = backup.export()
            assertEquals(7, org.json.JSONObject(exported).getInt("version"))
            cards.delete(created.id)
            backup.restore(exported)
            assertEquals(listOf(created), cards.list())

            val legacy = org.json.JSONObject(exported).apply {
                put("version", 6)
                remove("regulation_cards")
            }.toString()
            backup.restore(legacy)
            assertTrue(cards.list().isEmpty())
        } finally { db.close() }
    }

    @Test fun cardLimitIsEnforcedWithoutLosingExistingCards() {
        val db = database()
        try {
            val cards = RegulationCardStore(db)
            repeat(RegulationCardRules.MAX_CARDS) { cards.save(null, "Carte $it", listOf("Action $it")) }
            assertThrows(IllegalArgumentException::class.java) {
                cards.save(null, "En trop", listOf("Action"))
            }
            assertEquals(RegulationCardRules.MAX_CARDS, cards.list().size)
        } finally { db.close() }
    }
}
