package io.github.adven27.concordion.extensions.exam.core.utils

import io.github.adven27.concordion.extensions.exam.core.parseDate
import io.github.adven27.concordion.extensions.exam.core.periodBy
import io.github.adven27.concordion.extensions.exam.core.toZonedDateTime
import mu.KLogging
import net.javacrumbs.jsonunit.core.ParametrizedMatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import java.lang.Character.isDigit
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAmount
import java.util.Date
import javax.xml.datatype.DatatypeFactory

class DateFormatMatcher : BaseMatcher<Any>(), ParametrizedMatcher {
    private lateinit var param: String

    override fun matches(item: Any): Boolean = try {
        (item as String).parseDate(param)
        true
    } catch (expected: Exception) {
        false
    }

    override fun describeTo(description: Description) {
        description.appendValue(param)
    }

    override fun describeMismatch(item: Any, description: Description) {
        description.appendText("The date is not properly formatted ").appendValue(param)
    }

    override fun setParameter(parameter: String) {
        this.param = parameter
    }
}

class DateWithin private constructor(private val now: Boolean) : BaseMatcher<Any>(), ParametrizedMatcher {
    private lateinit var period: TemporalAmount
    private lateinit var expected: ZonedDateTime
    private lateinit var pattern: String
    private var parseError = false

    override fun matches(item: Any): Boolean {
        val target = try {
            date(item)
        } catch (expected: Exception) {
            logger.warn("Parsing error: $item, expected to match pattern '$pattern'", expected)
            parseError = true
            return false
        }
        return isBetweenInclusive(expected.minus(period), expected.plus(period), target)
    }

    private fun date(item: Any): ZonedDateTime = when (item) {
        is ZonedDateTime -> item
        is LocalDateTime -> item.atZone(ZoneId.systemDefault())
        is Date -> item.toZonedDateTime()
        else -> item.toString().parseDate(pattern).toZonedDateTime()
    }

    private fun isBetweenInclusive(start: ZonedDateTime, end: ZonedDateTime, target: ZonedDateTime): Boolean =
        !target.isBefore(start) && !target.isAfter(end)

    override fun describeTo(description: Description) {
        description.appendValue(period)
    }

    override fun describeMismatch(item: Any, description: Description) {
        if (parseError) {
            description.appendText("The date is not properly formatted ").appendValue(pattern)
        } else {
            description.appendText("The date is not within ").appendValue(expected.minus(period)..expected.plus(period))
        }
    }

    override fun setParameter(p: String) {
        pattern = p.substring(1, p.indexOf("]"))
        var param = p.substring(pattern.length + 2)
        val within = param.substring(1, param.indexOf("]"))

        if (now) {
            expected = ZonedDateTime.now()
        } else {
            param = param.substring(within.length + 2)
            val date = param.substring(1, param.indexOf("]"))
            expected = date.parseDate(pattern).toZonedDateTime()
        }
        this.period = parsePeriod(within)
    }

    companion object : KLogging() {
        fun param() = DateWithin(false)
        fun now() = DateWithin(true)
        fun now(param: String) = DateWithin(true).apply { setParameter("[][$param]") }
    }
}

class XMLDateWithin : BaseMatcher<Any>(), ParametrizedMatcher {
    private lateinit var period: TemporalAmount

    override fun matches(item: Any): Boolean {
        val xmlGregorianCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(item as String)
        val actual = xmlGregorianCal.toGregorianCalendar().toZonedDateTime()
        val expected = ZonedDateTime.now()
        return isBetweenInclusive(expected.minus(period), expected.plus(period), actual)
    }

    private fun isBetweenInclusive(start: ZonedDateTime, end: ZonedDateTime, target: ZonedDateTime): Boolean {
        return !target.isBefore(start) && !target.isAfter(end)
    }

    override fun describeTo(description: Description) {
        description.appendValue(period)
    }

    override fun describeMismatch(item: Any, description: Description) {
        description.appendText("The date should be within ").appendValue(period)
    }

    override fun setParameter(within: String) {
        period = parsePeriod(within)
    }
}

fun parsePeriod(within: String): TemporalAmount {
    var i = 0
    while (i < within.length && isDigit(within[i])) {
        i++
    }
    return periodBy(
        within.substring(0, i).toInt(),
        within.substring(i, within.length).trim()
    )
}
