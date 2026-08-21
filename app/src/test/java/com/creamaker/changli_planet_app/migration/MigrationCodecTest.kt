package com.creamaker.changli_planet_app.migration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MigrationCodecTest {
    @Test
    fun archiveRoundTripPreservesPayloadWithoutSensitiveFields() {
        val payload = samplePayload()
        val archive = MigrationArchive(
            migrationId = "123e4567-e89b-12d3-a456-426614174000",
            sourceVersionCode = 33,
            createdAt = System.currentTimeMillis(),
            payloadDigest = MigrationCodec.payloadDigest(payload),
            payload = payload
        )

        val encoded = MigrationCodec.encode(archive)
        val decoded = MigrationCodec.decode(encoded)
        val json = encoded.toString(Charsets.UTF_8).lowercase()

        assertEquals(archive, decoded)
        assertFalse(json.contains("password"))
        assertFalse(json.contains("token"))
        assertFalse(json.contains("cookie"))
    }

    @Test
    fun validatorAcceptsValidArchive() {
        val payload = samplePayload()
        val archive = MigrationArchive(
            migrationId = "123e4567-e89b-12d3-a456-426614174000",
            sourceVersionCode = 33,
            createdAt = System.currentTimeMillis(),
            payloadDigest = MigrationCodec.payloadDigest(payload),
            payload = payload
        )

        MigrationArchiveValidator.validate(archive, archive.migrationId)
    }

    private fun samplePayload() = MigrationPayload(
        customCourses = listOf(
            MigrationCourse(
                courseName = "软件工程",
                classroom = "云塘 A101",
                teacher = "教师",
                weeks = listOf(1, 2, 3),
                start = 1,
                step = 2,
                weekday = 1,
                term = "2026-2027-1",
                studentId = "20260001"
            )
        ),
        ledgerItems = listOf(
            MigrationLedgerItem(
                sourceId = 1,
                name = "自行车",
                totalMoney = 500.0,
                dailyAverage = 1.0,
                startTime = "2026-8-21",
                picture = 1,
                username = "20260001"
            )
        ),
        preferences = MigrationPreferences("skin_default", "skin_default")
    )
}
