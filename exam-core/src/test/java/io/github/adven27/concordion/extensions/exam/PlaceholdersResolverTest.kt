package io.github.adven27.concordion.extensions.exam

import io.github.adven27.concordion.extensions.exam.core.resolveJson
import io.github.adven27.concordion.extensions.exam.core.resolveToObj
import io.github.adven27.concordion.extensions.exam.core.utils.toDate
import io.github.adven27.concordion.extensions.exam.core.utils.toString
import org.assertj.core.api.Assertions
import org.concordion.internal.FixtureInstance
import org.concordion.internal.OgnlEvaluator
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import java.time.LocalDateTime
import java.util.Date
import kotlin.test.assertEquals

class PlaceholdersResolverTest {
    private val eval = OgnlEvaluator(FixtureInstance(Object()))

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

        val expected = date.toString("dd.MM.yyyy HH:mm")
        assertThat(eval.resolveToObj("{{dateFormat value \"dd.MM.yyyy HH:mm\"}}").toString(), `is`(expected))
        assertThat(eval.resolveToObj("{{dateFormat (now plus='1 d') 'dd.MM.yyyy HH:mm' minus='1 d'}}").toString(), `is`(expected))
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
            `is`("\${json-unit.matches:formattedAndWithin}yyyy-MM-dd|param|1d|param|1951-05-13")
        )
    }

    @Test
    fun canUseJsonUnitMatcherAliasWithRegexp() {
        assertThat(
            eval.resolveJson("{{formattedAs 'yyyy-MM-dd\\'T\\'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS]'}}"),
            `is`("\${json-unit.matches:formattedAs}yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS]")
        )
        assertThat(
            eval.resolveJson("{{formattedAndWithinNow 'yyyy-MM-dd\\'T\\'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS]' '1d'}}"),
            `is`("\${json-unit.matches:formattedAndWithinNow}yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS]|param|1d")
        )
    }

    @Test
    fun examDateVariables() {
        val p = "dd.MM.yyyy'T'hh:mm:ss"
        assertThat(eval.resolveJson("{{now \"$p\"}}"), `is`(Date().toString(p)))
        Assertions.assertThat(eval.resolveToObj("{{now}}") as Date)
            .isCloseTo(Date(), 1000)
        Assertions.assertThat(eval.resolveToObj("{{now minus='1 d'}}") as Date)
            .isCloseTo(LocalDateTime.now().minusDays(1).toDate(), 1000)
    }
}
