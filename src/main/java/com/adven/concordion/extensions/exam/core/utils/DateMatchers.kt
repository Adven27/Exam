package com.adven.concordion.extensions.exam.core.utils

import com.adven.concordion.extensions.exam.core.parseDate
import com.adven.concordion.extensions.exam.core.parseDateTime
import com.adven.concordion.extensions.exam.core.periodBy
import net.javacrumbs.jsonunit.core.ParametrizedMatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.joda.time.DateTime
import org.joda.time.base.BaseSingleFieldPeriod
import java.lang.Character.isDigit
import javax.xml.datatype.DatatypeFactory

class DateFormatMatcher : BaseMatcher<Any>(), ParametrizedMatcher {
    private var param: String? = null

    override fun matches(item: Any): Boolean = try {
        (item as String).parseDate(param!!)
        true
    } catch (e: Exception) {
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
    private var period: BaseSingleFieldPeriod? = null
    private lateinit var expected: DateTime
    private lateinit var pattern: String

    override fun matches(item: Any): Boolean {
        val actual = if (item is DateTime) item else (item as String).parseDateTime(pattern)
        return isBetweenInclusive(expected.minus(period), expected.plus(period), actual)
    }

    private fun isBetweenInclusive(start: DateTime, end: DateTime, target: DateTime): Boolean {
        return !target.isBefore(start) && !target.isAfter(end)
    }

    override fun describeTo(description: Description) {
        description.appendValue(period)
    }

    override fun describeMismatch(item: Any, description: Description) {
        description.appendText("The date should be within ").appendValue(period)
    }

    override fun setParameter(p: String) {
        pattern = p.substring(1, p.indexOf("]"))
        var param = p.substring(pattern.length + 2)
        val within = param.substring(1, param.indexOf("]"))

        if (now) {
            expected = DateTime.now()
        } else {
            param = param.substring(within.length + 2)
            val date = param.substring(1, param.indexOf("]"))
            expected = date.parseDateTime(pattern)
        }

        this.period = parsePeriod(within)
    }

    companion object {
        fun param() = DateWithin(false)
        fun now() = DateWithin(true)
        fun now(param: String) = DateWithin(true).apply { setParameter("[][$param]") }
    }
}

class XMLDateWithin : BaseMatcher<Any>(), ParametrizedMatcher {
    private var period: BaseSingleFieldPeriod? = null

    override fun matches(item: Any): Boolean {
        val xmlGregorianCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(item as String)
        val actual = DateTime(xmlGregorianCal.toGregorianCalendar())
        val expected = DateTime.now()
        return isBetweenInclusive(expected.minus(period), expected.plus(period), actual)
    }

    private fun isBetweenInclusive(start: DateTime, end: DateTime, target: DateTime): Boolean {
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

fun parsePeriod(within: String): BaseSingleFieldPeriod {
    var i = 0
    while (i < within.length && isDigit(within[i])) {
        i++
    }
    return periodBy(
        within.substring(0, i).toInt(),
        within.substring(i, within.length).trim()
    )
}