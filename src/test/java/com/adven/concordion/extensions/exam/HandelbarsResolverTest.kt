package com.adven.concordion.extensions.exam

import com.adven.concordion.extensions.exam.core.resolveJson
import com.adven.concordion.extensions.exam.core.resolveToObj
import com.adven.concordion.extensions.exam.core.utils.HANDLEBARS
import com.adven.concordion.extensions.exam.core.utils.HelperSource
import com.adven.concordion.extensions.exam.core.utils.HelperSource.Companion.DEFAULT_FORMAT
import com.adven.concordion.extensions.exam.core.utils.HelperSource.Companion.HANDELBAR_RESULT
import com.adven.concordion.extensions.exam.core.utils.resolve
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.HandlebarsException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.concordion.internal.OgnlEvaluator
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
import kotlin.test.assertEquals

class HandlebarsResolverTest {
    private val eval = OgnlEvaluator(null)
    private val sut: Handlebars = HANDLEBARS
    private val defaultFormat = DEFAULT_FORMAT.format()

    @Test
    fun dateFormat_defaults() {
        val expected = "2019-06-30T09:10:00"
        eval.setVariable("#someDate", LocalDateTime.parse(expected, defaultFormat).toDate())

        assertEquals(expected, sut("{{dateFormat someDate}}"))
    }

    @Test
    fun dateFormat_wrongContext() {
        val placeholder = """{{dateFormat someDate "yyyy-MM-dd" tz="GMT+3"}}"""

        assertThatExceptionOfType(HandlebarsException::class.java).isThrownBy { sut(placeholder) }
            .withMessageContaining("""Wrong context for helper '$placeholder', found 'null', expected instance of Date: ${HelperSource.dateFormat.example}""")
            .withRootCauseExactlyInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun dateFormat_wrongOptions() {
        eval.setVariable("#someDate", Date())
        val placeholder = """{{dateFormat someDate wrong="yyyy-MM-dd" tz="GMT+3"}}"""

        assertThatExceptionOfType(HandlebarsException::class.java).isThrownBy { sut(placeholder) }
            .withMessageContaining("""Wrong options for helper '$placeholder': found '[wrong]', expected any of '${HelperSource.dateFormat.opts}""")
            .withRootCauseExactlyInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun now_defaults() {
        assertThat(SimpleDateFormat("E MMM dd HH:mm:ss z yyyy").parse(sut("{{now}}")))
            .isCloseTo(Date(), 2000)

        assertThat(eval.getVariable(HANDELBAR_RESULT) as Date)
            .isCloseTo(Date(), 2000)

    }

    @Test
    fun now_wrongOptions() {
        val placeholder = """{{now wrong="yyyy-MM-dd" tz="GMT+3"}}"""

        assertThatExceptionOfType(HandlebarsException::class.java).isThrownBy { sut(placeholder) }
            .withMessageContaining("""Wrong options for helper '$placeholder': found '[wrong]', expected any of '${HelperSource.now.opts}""")
            .withRootCauseExactlyInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun defaults() {
        HelperSource.values().forEach { assertEquals(it.expected, sut(it)) }
    }

    private fun sut(h: HelperSource): Any? {
        h.context.forEach { (t, u) -> eval.setVariable("#$t", u) }
        return eval.resolveToObj(h.example)
    }

    private fun String.format() = DateTimeFormat.forPattern(this)

    private fun sut(placeholder: String) = sut.resolve(eval, placeholder)
}