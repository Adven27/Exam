package io.github.adven27.concordion.extensions.exam

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.HandlebarsException
import io.github.adven27.concordion.extensions.exam.core.handlebars.ExamHelper
import io.github.adven27.concordion.extensions.exam.core.handlebars.HANDLEBARS
import io.github.adven27.concordion.extensions.exam.core.handlebars.date.DateHelpers
import io.github.adven27.concordion.extensions.exam.core.handlebars.date.DateHelpers.Companion.DEFAULT_FORMAT
import io.github.adven27.concordion.extensions.exam.core.handlebars.matchers.MatcherHelpers
import io.github.adven27.concordion.extensions.exam.core.handlebars.resolve
import io.github.adven27.concordion.extensions.exam.core.handlebars.resolveObj
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.parseDate
import io.github.adven27.concordion.extensions.exam.core.resolveToObj
import io.github.adven27.concordion.extensions.exam.core.toDate
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.concordion.internal.FixtureInstance
import org.concordion.internal.OgnlEvaluator
import org.junit.Test
import java.time.LocalDate
import java.util.Date
import kotlin.test.assertEquals

class HandlebarsResolverTest {
    private val eval = OgnlEvaluator(FixtureInstance(Html("div").el))
    private val sut: Handlebars = HANDLEBARS
    private val defaultFormat = DEFAULT_FORMAT.format()

    @Test
    fun resolve_with_vars() {
        assertEquals(
            "var = v, date = ${LocalDate.now().toDate()}",
            sut("{{resolve 'var = {{var}}, date = {{td}}' var='v' td='{{today}}'}}")
        )
        assertEquals(
            "today is ${LocalDate.now().toDate()}; var1 is v; var2 is ${LocalDate.now().toDate()}",
            sut("{{resolveFile '/hb/resolve-file-vars.txt' var1='v' var2='{{today}}'}}")
        )
    }

    @Test
    fun date_defaults() {
        assertEquals(
            "2019-06-30T09:10:00".parseDate("yyyy-MM-dd'T'HH:mm:ss"),
            sutObj("{{date \"2019-06-30T09:10:00\"}}")
        )
        assertEquals(
            "2019-06-30T00:00:00".parseDate("yyyy-MM-dd'T'HH:mm:ss"),
            sutObj("{{date \"2019-06-30\"}}")
        )
    }

    @Test
    fun dateFormat_defaults() {
        val expected = "2019-06-30T09:10:00"
        eval.setVariable("#someDate", expected.parseDate(defaultFormat))

        assertEquals(expected, sut("{{dateFormat someDate}}"))
    }

    @Test
    fun dateFormat_wrongContext() {
        assertThatExceptionOfType(HandlebarsException::class.java)
            .isThrownBy { sut("""{{dateFormat someDate "yyyy-MM-dd" tz="GMT+3"}}""") }
    }

    @Test
    fun dateFormat_wrongOptions() {
        eval.setVariable("#someDate", Date())
        val placeholder = """{{dateFormat someDate wrong="yyyy-MM-dd" tz="GMT+3"}}"""

        assertThatExceptionOfType(HandlebarsException::class.java).isThrownBy { sut(placeholder) }
            .withMessageContaining("""Wrong options for helper '$placeholder': found '[wrong]', expected any of '${DateHelpers.dateFormat.options}""")
            .withRootCauseExactlyInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun now_defaults() {
        assertThat(eval.resolveToObj("{{now}}") as Date).isCloseTo(Date(), 2000)
    }

    @Test
    fun now_wrongOptions() {
        val placeholder = """{{now wrong="yyyy-MM-dd" tz="GMT+3"}}"""

        assertThatExceptionOfType(HandlebarsException::class.java).isThrownBy { sut(placeholder) }
            .withMessageContaining("""Wrong options for helper '$placeholder': found '[wrong]', expected any of '${DateHelpers.now.options}""")
            .withRootCauseExactlyInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `defaults date`() {
        DateHelpers.values().forEach {
            val expected = it.expected
            if (expected is Date) assertThat(helper(it) as Date).describedAs("Failed helper: %s", it)
                .isCloseTo(expected, 2000)
            else assertEquals(expected, helper(it), "Failed helper: $it")
        }
    }

    @Test
    fun `defaults matcher`() {
        MatcherHelpers.values().forEach {
            val expected = it.expected
            assertEquals(expected, helper(it), "Failed helper: $it")
        }
    }

    private fun helper(h: ExamHelper): Any? {
        h.context.forEach { (t, u) -> eval.setVariable("#$t", u) }
        return eval.resolveToObj(h.example)
    }

    private fun sut(placeholder: String) = sut.resolve(eval, placeholder)
    private fun sutObj(placeholder: String) = sut.resolveObj(eval, placeholder)
}
