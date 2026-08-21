package com.zhelearn.CSUSTPlanet.migration

import com.google.gson.Gson
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Test

class DigestCompatTest {
    /** 旧实现：整体序列化成 String 再摘要。 */
    private fun legacyDigest(payload: MigrationPayload): String =
        MessageDigest.getInstance("SHA-256")
            .digest(Gson().toJson(payload).toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    @Test
    fun streamingDigestMatchesLegacyDigest() {
        val payload = MigrationPayload(
            customCourses = (1..300).map {
                MigrationCourse("课程$it", "教室$it", "老师$it", listOf(1, 2, 3), 1, 2, 1, "2026-2027-1", "2026000$it")
            },
            ledgerItems = (1..300).map {
                MigrationLedgerItem(it, "条目$it", 12.5 * it, 1.5, "2026-8-21", 1, "user$it")
            },
            preferences = MigrationPreferences("skin_default", "skin_default")
        )
        assertEquals(legacyDigest(payload), MigrationCodec.payloadDigest(payload))
    }

    @Test
    fun streamingDigestMatchesLegacyForUnicodeAndEmptyPayload() {
        val payload = MigrationPayload(
            customCourses = emptyList(),
            ledgerItems = emptyList(),
            preferences = MigrationPreferences("skin_default", "skin_dark.apk")
        )
        assertEquals(legacyDigest(payload), MigrationCodec.payloadDigest(payload))
    }
}
