package com.adven.concordion.extensions.exam

import com.adven.concordion.extensions.exam.core.resolveJson
import com.adven.concordion.extensions.exam.core.resolveToObj
import org.assertj.core.api.Assertions
import org.concordion.internal.OgnlEvaluator
import org.hamcrest.Matchers.`is`
import org.joda.time.LocalDateTime.fromDateFields
import org.joda.time.LocalDateTime.now
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat.forPattern
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals


class PlaceholdersResolverTest {
    private val eval = OgnlEvaluator(Object())
    private val y4m4d4h4 = Period().plusDays(4).plusMonths(4).plusYears(4).plusHours(4)
    private val y3m3d3 = Period().plusDays(3).plusMonths(3).plusYears(3)
    private val m1d1 = Period().plusDays(1).plusMonths(1)

    @Test
    fun rangeAscent() {
        assertThat(
            (eval.resolveToObj("1..5") as IntProgression).toList(),
            `is`(listOf(1, 2, 3, 4, 5))
        )
    }

    @Test
    fun rangeDescent() {
        assertThat(
            (eval.resolveToObj("5..1") as IntProgression).toList(),
            `is`(listOf(5, 4, 3, 2, 1))
        )
    }

    @Test
    fun canUseConcordionVars() {
        eval.setVariable("#value", 3)

        assertThat(eval.resolveJson("\${#value}"), `is`("3"))
        assertThat(eval.resolveJson("{{value}}"), `is`("3"))
        assertEquals(eval.resolveToObj("{{value}}"), 3)
    }

    @Test
    fun canFormatConcordionVars() {
        val date = Date()
        eval.setVariable("#value", date)

        val expected = forPattern("dd.MM.yyyy HH:mm:ss").print(fromDateFields(date))
        assertThat(eval.resolveJson("\${#value:dd.MM.yyyy HH:mm:ss}"), `is`(expected))
    }

    @Test
    fun resolveToObj_shouldResolveFormattedConcordionVarToString() {
        val date = Date()
        eval.setVariable("#value", date)

        val expected = forPattern("dd.MM.yyyy HH:mm:ss").print(fromDateFields(date))
        assertThat(eval.resolveToObj("\${#value:dd.MM.yyyy HH:mm:ss}").toString(), `is`(expected))
        assertThat(eval.resolveToObj("{{dateFormat value \"dd.MM.yyyy HH:mm:ss\"}}").toString(), `is`(expected))
        assertThat(eval.resolveToObj("{{dateFormat (now plus='1 d') 'dd.MM.yyyy HH:mm:ss' minus='1 d'}}").toString(), `is`(expected))
    }

    @Test
    fun canUseJsonUnitStringAliases() {
        val expected = "\${json-unit.any-string}"
        assertThat(eval.resolveJson("!{any-string}"), `is`(expected))
        assertThat(eval.resolveJson("!{aNy-stRiNG}"), `is`(expected))
        assertThat(eval.resolveJson("!{string}"), `is`(expected))
        assertThat(eval.resolveJson("!{str}"), `is`(expected))
        assertThat(eval.resolveJson("{{string}}"), `is`(expected))
    }

    @Test
    fun canUseJsonUnitNumberAliases() {
        val expected = "\${json-unit.any-number}"
        assertThat(eval.resolveJson("!{any-number}"), `is`(expected))
        assertThat(eval.resolveJson("!{aNy-nuMBeR}"), `is`(expected))
        assertThat(eval.resolveJson("!{number}"), `is`(expected))
        assertThat(eval.resolveJson("!{num}"), `is`(expected))
        assertThat(eval.resolveJson("{{number}}"), `is`(expected))
    }

    @Test
    fun canUseJsonUnitBoolAliases() {
        val expected = "\${json-unit.any-boolean}"
        assertThat(eval.resolveJson("!{any-boolean}"), `is`(expected))
        assertThat(eval.resolveJson("!{aNy-bOOlean}"), `is`(expected))
        assertThat(eval.resolveJson("!{boolean}"), `is`(expected))
        assertThat(eval.resolveJson("!{bool}"), `is`(expected))
        assertThat(eval.resolveJson("{{bool}}"), `is`(expected))
    }

    @Test
    fun canUseJsonUnitMatcherAliases() {
        assertThat(
            eval.resolveJson("!{formattedAs dd.MM.yyyy}"),
            `is`("\${json-unit.matches:formattedAs}dd.MM.yyyy")
        )
        assertThat(
            eval.resolveJson("!{formattedAs dd.MM.yyyy HH:mm}"),
            `is`("\${json-unit.matches:formattedAs}dd.MM.yyyy HH:mm")
        )
        assertThat(
            eval.resolveJson("!{formattedAndWithin [yyyy-MM-dd][1d][1951-05-13]}"),
            `is`("\${json-unit.matches:formattedAndWithin}[yyyy-MM-dd][1d][1951-05-13]")
        )
    }

    @Test
    fun canAddSimplePeriodToNow() {
        val expected = now().plusDays(1).toString("dd.MM.yyyy")

        assertThat(eval.resolveJson("\${exam.now+[1 d]:dd.MM.yyyy}"), `is`(expected))
        assertThat(eval.resolveJson("\${exam.now+[1 day]:dd.MM.yyyy}"), `is`(expected))
        assertThat(eval.resolveJson("\${exam.now+[day 1]:dd.MM.yyyy}"), `is`(expected))
        assertThat(eval.resolveJson("\${exam.now+[1 days]:dd.MM.yyyy}"), `is`(expected))
    }

    @Test
    fun canAddCompositePeriodsToNow() {
        assertThat(
            eval.resolveJson("\${exam.now+[1 day, 1 month]:dd.MM.yyyy}"),
            `is`(now().plus(m1d1).toString("dd.MM.yyyy"))
        )
        assertThat(
            eval.resolveJson("\${exam.now+[days 3, months 3, 3 years]:dd.MM.yyyy}"),
            `is`(now().plus(y3m3d3).toString("dd.MM.yyyy"))
        )
        assertThat(
            eval.resolveJson("\${exam.now+[4 d, 4 M, y 4, 4 hours]:dd.MM.yyyy'T'hh}"),
            `is`(now().plus(y4m4d4h4).toString("dd.MM.yyyy'T'hh"))
        )
    }

    @Test
    fun canSubtractCompositePeriodsFromNow() {
        assertThat(
            eval.resolveJson("\${exam.now-[1 day, 1 month]:dd.MM.yyyy}"),
            `is`(now().minus(m1d1).toString("dd.MM.yyyy"))
        )
        assertThat(
            eval.resolveJson("\${exam.now-[days 3, months 3, 3 years]:dd.MM.yyyy}"),
            `is`(now().minus(y3m3d3).toString("dd.MM.yyyy"))
        )
        assertThat(
            eval.resolveJson("\${exam.now-[4 d, 4 M, y 4, 4 hours]:dd.MM.yyyy'T'hh}"),
            `is`(now().minus(y4m4d4h4).toString("dd.MM.yyyy'T'hh"))
        )
    }

    @Test
    fun examDateVariables() {
        val p = "dd.MM.yyyy'T'hh:mm:ss"
        assertThat(eval.resolveJson("\${exam.now:$p}"), `is`(now().toString(p)))
        assertThat(eval.resolveJson("{{now \"$p\"}}"), `is`(now().toString(p)))
        Assertions.assertThat(eval.resolveToObj("{{now}}") as Date)
            .isCloseTo(now().toDate(), 1000)
        Assertions.assertThat(eval.resolveToObj("{{now minus='1 d'}}") as Date)
            .isCloseTo(now().minusDays(1).toDate(), 1000)
        assertThat(eval.resolveJson("\${exam.tomorrow:$p}"), `is`(now().plusDays(1).toString(p)))
        assertThat(eval.resolveJson("\${exam.yesterday:$p}"), `is`(now().minusDays(1).toString(p)))
    }
}