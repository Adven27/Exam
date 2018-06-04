package com.adven.concordion.extensions.exam.db.commands

import org.junit.Assert.assertEquals
import org.junit.Test

class DBCommandTest {

    @Test
    fun canParseColsDescription() {
        val res = parse("NOTHING, **MARKED, HAS_VAL='', *MARKED_AND_VAL=42")
        assertEquals(res.first, mapOf("NOTHING" to 0, "MARKED" to 2, "HAS_VAL" to 0, "MARKED_AND_VAL" to 1))
        assertEquals(res.second, mapOf("NOTHING" to "", "MARKED" to "", "HAS_VAL" to "''", "MARKED_AND_VAL" to "42"))
    }

    @Test
    fun canParseSingleFieldColsDescription() {
        val res = parse("some_column")
        assertEquals(res.first, mapOf("some_column" to 0))
        assertEquals(res.second, mapOf("some_column" to ""))
    }
}