package io.github.adven27.concordion.extensions.exam.core.utils

import mu.KLogging
import net.javacrumbs.jsonunit.core.ParametrizedMatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import java.time.ZonedDateTime
import java.time.temporal.TemporalAmount
import java.util.Date
import javax.xml.datatype.DatatypeFactory

open class DateFormatMatcher(var pattern: String? = null) : BaseMatcher<Any>(), ParametrizedMatcher {
    override fun matches(item: Any): Boolean = date(item, pattern).map { true }.getOrDefault(false)

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

    override fun matches(item: Any) = date(item, pattern)
        .map { isBetweenInclusive(expected.minus(period), expected.plus(period), it) }
        .getOrElse {
            logger.warn("Parsing error: $item, expected to match pattern '$pattern'", it)
            parseError = true
            false
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

    override fun matches(item: Any) = date(item).map { check(expected, it) }.getOrDefault(false)

    override fun describeTo(description: Description) {
        description.appendValue(expected)
    }

    override fun describeMismatch(item: Any, description: Description) {
        description.appendText(mismatchDesc).appendValue(expected)
    }

    override fun setParameter(date: String) {
        expected = date(date).getOrThrow()
    }
}

fun Any.asString(): String = when (this) {
    is Date -> toZonedDateTime().toString()
    else -> toString()
}
