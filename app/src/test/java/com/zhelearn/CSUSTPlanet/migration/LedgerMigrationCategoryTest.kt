package com.zhelearn.CSUSTPlanet.migration

import com.zhelearn.CSUSTPlanet.R
import org.junit.Assert.assertEquals
import org.junit.Test

class LedgerMigrationCategoryTest {
    @Test
    fun phoneUsesStableCategoryAcrossExportAndImport() {
        val category = LedgerMigrationCategory.fromDrawable(R.drawable.ic_iphone)

        assertEquals(LedgerMigrationCategory.PHONE, category)
        assertEquals(R.drawable.ic_iphone, LedgerMigrationCategory.toDrawable(category))
    }

    @Test
    fun unknownDrawableBecomesOther() {
        val category = LedgerMigrationCategory.fromDrawable(Int.MIN_VALUE)

        assertEquals(LedgerMigrationCategory.OTHER, category)
        assertEquals(R.drawable.nfeature, LedgerMigrationCategory.toDrawable(category))
    }
}
