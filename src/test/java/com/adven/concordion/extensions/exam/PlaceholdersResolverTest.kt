package com.adven.concordion.extensions.exam

import com.adven.concordion.extensions.exam.core.resolveJson
import com.adven.concordion.extensions.exam.core.resolveToObj
import com.adven.concordion.extensions.exam.core.toStringOr
import org.assertj.core.api.Assertions
import org.concordion.internal.OgnlEvaluator
import org.hamcrest.Matchers.`is`
import org.joda.time.LocalDateTime.fromDateFields
import org.joda.time.LocalDateTime.now
import org.joda.time.format.DateTimeFormat.forPattern
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.Date
import kotlin.test.assertEquals


class PlaceholdersResolverTest {
    private val eval = OgnlEvaluator(Object())

    @Test
    fun canUseConcordionVars() {
        eval.setVariable("#value", 3)

        assertThat(eval.resolveJson("{{value}}"), `is`("3"))
        assertEquals(eval.resolveToObj("{{value}}"), 3)
    }

    @Test
    fun resolveToObj_shouldResolveFormattedConcordionVarToString() {
        val date = Date()
        eval.setVariable("#value", date)

        val expected = forPattern("dd.MM.yyyy HH:mm").print(fromDateFields(date))
        assertThat(eval.resolveToObj("{{dateFormat value \"dd.MM.yyyy HH:mm\"}}").toString(), `is`(expected))
        assertThat(eval.resolveToObj("{{dateFormat (now plus='1 d') 'dd.MM.yyyy HH:mm' minus='1 d'}}").toString(), `is`(expected))
    }

    @Test
    fun `resolveToObj should skip resolving if starts and ends with apostrophe`() {
        assertThat(eval.resolveToObj(" '{{resolve \"some\"}}'").toStringOr(""), `is`("some"))
        assertThat(eval.resolveToObj("'{{resolve \"some\"}}' ").toStringOr(""), `is`("some"))
        assertThat(eval.resolveToObj("'{{resolve \"some\"}}'").toStringOr(""), `is`("{{resolve \"some\"}}"))
    }

    @Test
    fun canUseJsonUnitMatcherAliases() {
        assertThat(
            eval.resolveJson("{{formattedAs 'dd.MM.yyyy'}}"),
            `is`("\${json-unit.matches:formattedAs}dd.MM.yyyy")
        )
        assertThat(
            eval.resolveJson("{{formattedAs 'dd.MM.yyyy HH:mm'}}"),
            `is`("\${json-unit.matches:formattedAs}dd.MM.yyyy HH:mm")
        )
        assertThat(
            eval.resolveJson("{{formattedAndWithin 'yyyy-MM-dd' '1d' '1951-05-13'}}"),
            `is`("\${json-unit.matches:formattedAndWithin}[yyyy-MM-dd][1d][1951-05-13]")
        )
    }

    @Test
    fun examDateVariables() {
        val p = "dd.MM.yyyy'T'hh:mm:ss"
        assertThat(eval.resolveJson("{{now \"$p\"}}"), `is`(now().toString(p)))
        Assertions.assertThat(eval.resolveToObj("{{now}}") as Date)
            .isCloseTo(now().toDate(), 1000)
        Assertions.assertThat(eval.resolveToObj("{{now minus='1 d'}}") as Date)
            .isCloseTo(now().minusDays(1).toDate(), 1000)
    }
}