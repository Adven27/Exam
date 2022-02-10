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

open class DateFormatMatcher(var pattern: String? = null) : BaseMatcher<Any>(), ParametrizedMatcher {
    override fun matches(item: Any): Boolean = try {
        (item as String).parseDate(pattern)
        true
    } catch (expected: Exception) {
        false
    }

    override fun describeTo(description: Description) {
        description.appendValue(pattern)
    }

    override fun describeMismatch(item: Any, description: Description) {
        description.appendText("The date is not properly formatted ").appendValue(pattern)
    }

    override fun setParameter(parameter: String) {
        if (parameter.isNotBlank()) {
            this.pattern = parameter
        }
    }
}

class DateWithinNow : DateWithin()
class DateWithinParam : DateWithin(false)

open class DateWithin(
    private val now: Boolean = true,
    var pattern: String? = null
) : BaseMatcher<Any>(), ParametrizedMatcher {
    private lateinit var period: TemporalAmount
    private lateinit var expected: ZonedDateTime
    private var parseError = false

    override fun matches(item: Any): Boolean {
        val target = try {
            date(item, pattern)
        } catch (expected: Exception) {
            logger.warn("Parsing error: $item, expected to match pattern '$pattern'", expected)
            parseError = true
            return false
        }
        return isBetweenInclusive(expected.minus(period), expected.plus(period), target)
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
        val params = p.split(PARAMS_SEPARATOR)
        if (params[0].isNotBlank()) {
            this.pattern = params[0]
        }
        this.period = parsePeriod(params[1])
        this.expected = if (now) ZonedDateTime.now() else params[2].parseDate().toZonedDateTime()
    }

    companion object : KLogging() {
        internal const val PARAMS_SEPARATOR = "|param|"
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

    private fun isBetweenInclusive(start: ZonedDateTime, end: ZonedDateTime, target: ZonedDateTime): Boolean =
        !target.isBefore(start) && !target.isAfter(end)

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

class After : ExpectedDateMatcher("The date should be after ", { expected, actual -> actual.isAfter(expected) })
class Before : ExpectedDateMatcher("The date should be before ", { expected, actual -> actual.isBefore(expected) })

abstract class ExpectedDateMatcher(
    private val mismatchDesc: String,
    val check: (expected: ZonedDateTime, actual: ZonedDateTime) -> Boolean
) : BaseMatcher<Any>(), ParametrizedMatcher {
    protected lateinit var expected: ZonedDateTime

    override fun matches(item: Any): Boolean {
        val actual = try {
            date(item)
        } catch (expected: Exception) {
            return false
        }
        return check(expected, actual)
    }

    override fun describeTo(description: Description) {
        description.appendValue(expected)
    }

    override fun describeMismatch(item: Any, description: Description) {
        description.appendText(mismatchDesc).appendValue(expected)
    }

    override fun setParameter(date: String) {
        expected = date.parseDate().toZonedDateTime()
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

fun date(item: Any, pattern: String? = null): ZonedDateTime = when (item) {
    is ZonedDateTime -> item
    is LocalDateTime -> item.atZone(ZoneId.systemDefault())
    is Date -> item.toZonedDateTime()
    else -> item.toString().parseDate(pattern).toZonedDateTime()
}

fun Any.asString(): String = when (this) {
    is Date -> toZonedDateTime().toString()
    else -> toString()
}
