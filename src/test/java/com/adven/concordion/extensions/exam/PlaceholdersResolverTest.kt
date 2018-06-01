package com.adven.concordion.extensions.exam

import com.adven.concordion.extensions.exam.db.Range
import org.concordion.api.Evaluator
import org.joda.time.Period
import org.junit.Test

import java.util.Date

import com.adven.concordion.extensions.exam.PlaceholdersResolver.resolveJson
import com.adven.concordion.extensions.exam.PlaceholdersResolver.resolveToObj
import net.sf.qualitycheck.Throws
import org.hamcrest.Matchers.`is`
import org.joda.time.LocalDateTime.fromDateFields
import org.joda.time.LocalDateTime.now
import org.joda.time.base.BasePeriod
import org.joda.time.format.DateTimeFormat.forPattern
import org.junit.Assert.assertThat
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class PlaceholdersResolverTest {
    private val eval = mock<Evaluator>(Evaluator::class.java)
    private val y4m4d4h4 = Period().plusDays(4).plusMonths(4).plusYears(4).plusHours(4)
    private val y3m3d3 = Period().plusDays(3).plusMonths(3).plusYears(3)
    private val m1d1 = Period().plusDays(1).plusMonths(1)

    @Test
    @Throws(Exception::class)
    fun rangeAscent() {
        val actual = resolveToObj("1..5", eval) as Range
        assertThat(actual.get(0), `is`(1))
        assertThat(actual.get(2), `is`(3))
        assertThat(actual.get(5), `is`(1))
    }

    @Test
    @Throws(Exception::class)
    fun rangeDescent() {
        val actual = resolveToObj("5..1", eval) as Range
        assertThat(actual.get(0), `is`(5))
        assertThat(actual.get(2), `is`(3))
        assertThat(actual.get(5), `is`(5))
    }

    @Test
    @Throws(Exception::class)
    fun canUseConcordionVars() {
        `when`(eval.getVariable("#value")).thenReturn(3)

        assertThat(resolveJson("\${#value}", eval), `is`("3"))
    }

    @Test
    @Throws(Exception::class)
    fun canFormatConcordionVars() {
        val date = Date()
        `when`(eval.getVariable("#value")).thenReturn(date)

        val expected = forPattern("dd.MM.yyyy HH:mm:ss").print(fromDateFields(date))
        assertThat(resolveJson("\${#value:dd.MM.yyyy HH:mm:ss}", eval), `is`(expected))
    }

    @Test
    @Throws(Exception::class)
    fun resolveToObj_shouldResolveFormattedConcordionVarToString() {
        val date = Date()
        `when`(eval.getVariable("#value")).thenReturn(date)

        val expected = forPattern("dd.MM.yyyy HH:mm:ss").print(fromDateFields(date))
        assertThat(resolveToObj("\${#value:dd.MM.yyyy HH:mm:ss}", eval).toString(), `is`(expected))
    }

    @Test
    @Throws(Exception::class)
    fun canUseJsonUnitStringAliases() {
        val expected = "\${json-unit.any-string}"
        assertThat(resolveJson("!{any-string}", eval), `is`(expected))
        assertThat(resolveJson("!{aNy-stRiNG}", eval), `is`(expected))
        assertThat(resolveJson("!{string}", eval), `is`(expected))
        assertThat(resolveJson("!{str}", eval), `is`(expected))
    }

    @Test
    @Throws(Exception::class)
    fun canUseJsonUnitNumberAliases() {
        val expected = "\${json-unit.any-number}"
        assertThat(resolveJson("!{any-number}", eval), `is`(expected))
        assertThat(resolveJson("!{aNy-nuMBeR}", eval), `is`(expected))
        assertThat(resolveJson("!{number}", eval), `is`(expected))
        assertThat(resolveJson("!{num}", eval), `is`(expected))
    }

    @Test
    @Throws(Exception::class)
    fun canUseJsonUnitBoolAliases() {
        val expected = "\${json-unit.any-boolean}"
        assertThat(resolveJson("!{any-boolean}", eval), `is`(expected))
        assertThat(resolveJson("!{aNy-bOOlean}", eval), `is`(expected))
        assertThat(resolveJson("!{boolean}", eval), `is`(expected))
        assertThat(resolveJson("!{bool}", eval), `is`(expected))
    }

    @Test
    @Throws(Exception::class)
    fun canUseJsonUnitMatcherAliases() {
        assertThat(resolveJson("!{formattedAs dd.MM.yyyy}", eval),
                `is`("\${json-unit.matches:formattedAs}dd.MM.yyyy"))
        assertThat(resolveJson("!{formattedAndWithin [yyyy-MM-dd][1d][1951-05-13]}", eval),
                `is`("\${json-unit.matches:formattedAndWithin}[yyyy-MM-dd][1d][1951-05-13]"))
    }

    @Test
    @Throws(Exception::class)
    fun canAddSimplePeriodToNow() {
        val expected = now().plusDays(1).toString("dd.MM.yyyy")

        assertThat(resolveJson("\${exam.now+[1 d]:dd.MM.yyyy}", eval), `is`(expected))
        assertThat(resolveJson("\${exam.now+[1 day]:dd.MM.yyyy}", eval), `is`(expected))
        assertThat(resolveJson("\${exam.now+[day 1]:dd.MM.yyyy}", eval), `is`(expected))
        assertThat(resolveJson("\${exam.now+[1 days]:dd.MM.yyyy}", eval), `is`(expected))
    }

    @Test
    @Throws(Exception::class)
    fun canAddCompositePeriodsToNow() {
        assertThat(resolveJson("\${exam.now+[1 day, 1 month]:dd.MM.yyyy}", eval),
                `is`(now().plus(m1d1).toString("dd.MM.yyyy")))
        assertThat(resolveJson("\${exam.now+[days 3, months 3, 3 years]:dd.MM.yyyy}", eval),
                `is`(now().plus(y3m3d3).toString("dd.MM.yyyy")))
        assertThat(resolveJson("\${exam.now+[4 d, 4 M, y 4, 4 hours]:dd.MM.yyyy'T'hh}", eval),
                `is`(now().plus(y4m4d4h4).toString("dd.MM.yyyy'T'hh")))
    }

    @Test
    @Throws(Exception::class)
    fun canSubtractCompositePeriodsFromNow() {
        assertThat(resolveJson("\${exam.now-[1 day, 1 month]:dd.MM.yyyy}", eval),
                `is`(now().minus(m1d1).toString("dd.MM.yyyy")))
        assertThat(resolveJson("\${exam.now-[days 3, months 3, 3 years]:dd.MM.yyyy}", eval),
                `is`(now().minus(y3m3d3).toString("dd.MM.yyyy")))
        assertThat(resolveJson("\${exam.now-[4 d, 4 M, y 4, 4 hours]:dd.MM.yyyy'T'hh}", eval),
                `is`(now().minus(y4m4d4h4).toString("dd.MM.yyyy'T'hh")))
    }
}