package io.github.adven27.concordion.extensions.exam.core.utils

import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DateWithinTest {

    companion object {
        private const val DATE_PATTERN = "yyyy-MM-dd"
        private const val DATE_WITH_REGEXP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS][.SSSSSSSS][.SSSSSSS][.SSSSSS][.SSSSS][.SSSS][.SSS][.SS][.S]"

        private val DELTA = Duration.ofMillis(3000)
    }

    @Test
    fun `should match date pattern`() {
        val sut = DateFormatMatcher().apply { setParameter(DATE_PATTERN) }

        assertTrue(sut.matches("1951-05-13"))
    }

    @Test
    fun `should match date pattern with regexp`() {
        val sut = DateFormatMatcher().apply { setParameter(DATE_WITH_REGEXP_PATTERN) }

        assertTrue(sut.matches("1951-05-13T16:07:15.239223"))
    }

    @Test
    fun `should match date pattern within period`() {
        val sut = DateWithinParam().apply { setParameter("$DATE_PATTERN|param|1d|param|1951-05-13") }

        assertTrue(sut.matches("1951-05-12"))
        assertTrue(sut.matches("1951-05-13"))
        assertTrue(sut.matches("1951-05-14"))
    }

    @Test
    fun `should not match date pattern outside of period`() {
        val sut = DateWithinParam().apply { setParameter("$DATE_PATTERN|param|1d|param|1951-05-13") }

        assertFalse(sut.matches("1951-05-11"))
        assertFalse(sut.matches("1951-05-15"))
    }

    @Test
    fun `should match date pattern with regexp within period`() {
        val sut = DateWithinParam().apply { setParameter("$DATE_WITH_REGEXP_PATTERN|param|1d|param|1951-05-13T16:07:15.239223") }

        assertTrue(sut.matches("1951-05-12T16:07:15.239223"))
        assertTrue(sut.matches("1951-05-13T16:07:15.239223"))
        assertTrue(sut.matches("1951-05-14T16:07:15.239223"))
    }

    @Test
    fun `should match date pattern with regexp outside of period`() {
        val sut = DateWithinParam().apply { setParameter("$DATE_WITH_REGEXP_PATTERN|param|1d|param|1951-05-13T16:07:15.239223") }

        assertFalse(sut.matches("1951-05-11T16:07:15.239223"))
        assertFalse(sut.matches("1951-05-15T16:07:15.239223"))
    }

    @Test
    fun `should match now date pattern with regexp within period`() {
        val sut = DateWithinNow().apply { setParameter("$DATE_WITH_REGEXP_PATTERN|param|1d") }
        val now = LocalDateTime.now()

        assertTrue(sut.matches(now.minusDays(1).plus(DELTA).format(ISO_DATE_TIME)))
        assertTrue(sut.matches(now.format(ISO_DATE_TIME)))
        assertTrue(sut.matches(now.plusDays(1).minus(DELTA).format(ISO_DATE_TIME)))
    }

    @Test
    fun `should match now date pattern with regexp outside of period`() {
        val sut = DateWithinNow().apply { setParameter("$DATE_WITH_REGEXP_PATTERN|param|1d") }
        val now = LocalDateTime.now()

        assertFalse(sut.matches(now.minusDays(2).plus(DELTA).format(ISO_DATE_TIME)))
        assertFalse(sut.matches(now.plusDays(2).minus(DELTA).format(ISO_DATE_TIME)))
    }
}
