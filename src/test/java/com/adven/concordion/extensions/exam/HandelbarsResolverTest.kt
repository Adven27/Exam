package com.adven.concordion.extensions.exam

import com.adven.concordion.extensions.exam.core.utils.EvaluatorValueResolver
import com.adven.concordion.extensions.exam.core.utils.HelperSource
import com.adven.concordion.extensions.exam.core.utils.missing
import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.HandlebarsException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.concordion.internal.OgnlEvaluator
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class HandlebarsResolverTest {
    private val eval = OgnlEvaluator(null)
    private val sut: Handlebars = Handlebars()
        .registerHelpers(HelperSource::class.java)
        .registerHelperMissing(missing)
        .prettyPrint(false)

    private val defaultFormat = "dd.MM.yyyy'T'HH:mm:ss".format()

    @Test
    fun dateFormat() {
        eval.setVariable(
            "#someDate",
            LocalDateTime.parse("2019-06-30 09:10 +0300", "yyyy-MM-dd HH:mm Z".format()).toDate()
        )

        assertEquals(
            "2019-07-01 08:10 +0300",
            sut("""{{dateFormat someDate format="yyyy-MM-dd HH:mm Z" tz="GMT+3" plus="1 d" minus="1 h"}}""")
        )
    }


    @Test
    fun dateFormat_defaults() {
        val expected = "30.06.2019T09:10:00"
        eval.setVariable("#someDate", LocalDateTime.parse(expected, defaultFormat).toDate())

        assertEquals(expected, sut("""{{dateFormat someDate}}"""))
    }

    @Test
    fun dateFormat_wrongContext() {
        val placeholder = """{{dateFormat someDate format="yyyy-MM-dd" tz="GMT+3"}}"""

        assertThatExceptionOfType(HandlebarsException::class.java).isThrownBy { sut(placeholder) }
            .withMessageContaining("""Wrong context for helper '$placeholder', found 'null', expected instance of Date: ${HelperSource.dateFormat.desc}""")
            .withRootCauseExactlyInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun dateFormat_wrongOptions() {
        eval.setVariable("#someDate", Date())
        val placeholder = """{{dateFormat someDate wrong="yyyy-MM-dd" tz="GMT+3"}}"""

        assertThatExceptionOfType(HandlebarsException::class.java).isThrownBy { sut(placeholder) }
            .withMessageContaining("""Wrong options for helper '$placeholder', found '[wrong]', expected any of '${HelperSource.dateFormat.opts}""")
            .withRootCauseExactlyInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun now() {
        assertEquals(
            DateTime(DateTimeZone.forOffsetHours(3))
                .plusMonths(1)
                .plusDays(1)
                .minusHours(1)
                .toString("yyyy-MM-dd HH:mm Z"),
            sut("""{{now format="yyyy-MM-dd HH:mm Z" tz="GMT+3" plus="1 month, 1 d" minus="1 h"}}""")
        )
    }

    @Test
    fun now_defaults() {
        assertThat(LocalDateTime.parse(sut("{{now}}"), defaultFormat).toDate())
            .isCloseTo(Date(), 2000)
    }

    @Test
    fun now_wrongOptions() {
        val placeholder = """{{now wrong="yyyy-MM-dd" tz="GMT+3"}}"""

        assertThatExceptionOfType(HandlebarsException::class.java).isThrownBy { sut(placeholder) }
            .withMessageContaining("""Wrong options for helper '$placeholder', found '[wrong]', expected any of '${HelperSource.now.opts}""")
            .withRootCauseExactlyInstanceOf(IllegalArgumentException::class.java)
    }

    private fun String.format() = DateTimeFormat.forPattern(this)

    private fun sut(placeholder: String) = sut.compileInline(placeholder).apply(
        Context.newBuilder(eval).resolver(EvaluatorValueResolver.INSTANCE).build()
    )
}