package org.lepotager.executivefunction

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SharedTextTest {
    @Test fun sharesAreTrimmedWithoutChangingText() {
        val intent = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,"  Lire https://example.org  ")
        assertEquals("Lire https://example.org", SharedText.from(intent))
    }
    @Test fun wrongActionMimeAndEmptyTextAreIgnored() {
        assertNull(SharedText.from(Intent(Intent.ACTION_VIEW).setType("text/plain").putExtra(Intent.EXTRA_TEXT,"hello")))
        assertNull(SharedText.from(Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_TEXT,"hello")))
        assertNull(SharedText.from(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT," ")))
    }
    @Test fun longSharesAreBounded() {
        val intent = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,"x".repeat(6000))
        assertEquals(SharedText.MAX_CHARS,SharedText.from(intent)?.length)
    }
}
