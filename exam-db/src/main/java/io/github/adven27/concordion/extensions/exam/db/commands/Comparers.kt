package io.github.adven27.concordion.extensions.exam.db.commands

import io.github.adven27.concordion.extensions.exam.core.resolveToObj
import io.github.adven27.concordion.extensions.exam.core.toLocalDateTime
import io.github.adven27.concordion.extensions.exam.core.utils.parsePeriod
import io.github.adven27.concordion.extensions.exam.db.RowComparator
import org.concordion.api.Evaluator
import org.dbunit.assertion.comparer.value.IsActualEqualToExpectedValueComparer
import org.dbunit.assertion.comparer.value.IsActualWithinToleranceOfExpectedTimestampValueComparer
import org.dbunit.dataset.ITable
import org.dbunit.dataset.SortedTable
import org.dbunit.dataset.datatype.DataType
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID
import java.util.regex.Pattern

open class ExamMatchersAwareValueComparer : IsActualEqualToExpectedValueComparer() {
    protected lateinit var evaluator: Evaluator

    fun setEvaluator(evaluator: Evaluator): ExamMatchersAwareValueComparer {
        this.evaluator = evaluator
        return this
    }

    override fun isExpected(
        expectedTable: ITable?,
        actualTable: ITable?,
        rowNum: Int,
        columnName: String?,
        dataType: DataType,
        expected: Any?,
        actual: Any?
    ): Boolean = when {
        expected.isError() -> false
        expected.isNumber() -> setVarAndCheck(actual, expected) { a, _ -> regexMatches("^\\d+\$", a) }
        expected.isString() -> setVarAndCheck(actual, expected) { a, _ -> regexMatches("^\\w+\$", a) }
        expected.isRegex() -> setVarAndCheck(actual, expected) { a, e -> regexMatches(e, a) }
        expected.isNotNull() -> setVarAndCheck(actual, expected) { a, _ -> a != null }
        expected.isUuid() -> setVarAndCheck(actual, expected) { a, _ -> isUuid(a) }
        expected.isWithin() -> setVarAndCheck(actual, expected) { a, e ->
            WithinValueComparer(expected.toString().withinPeriod()).isExpected(
                expectedTable, actualTable, rowNum, columnName, dataType, resolve(e.toString()), a
            )
        }
        else -> super.isExpected(expectedTable, actualTable, rowNum, columnName, dataType, expected, actual)
    }

    private fun isUuid(a: Any?) = a is String && try {
        UUID.fromString(a)
        true
    } catch (ignore: Exception) {
        false
    }

    private fun setVarAndCheck(
        actual: Any?,
        expected: Any?,
        check: (actual: Any?, expected: Any?) -> Boolean
    ): Boolean {
        val split = expected.toString().split(">>")
        if (split.size > 1) evaluator.setVariable("#${split[1]}", actual)
        return check(actual, split[0])
    }

    private fun resolve(expected: String): Timestamp = expected.expression().let {
        Timestamp((if (it.isBlank()) Date() else (evaluator.resolveToObj(it) as Date)).time)
    }

    private fun regexMatches(expectedValue: Any?, actualValue: Any?): Boolean {
        if (actualValue == null) return false
        return regexMatches(expectedValue.toString().expression(), actualValue)
    }

    private fun String.expression() = substring(indexOf("}") + 1).trim()

    private fun regexMatches(pattern: String, actualValue: Any?): Boolean =
        if (actualValue == null) false else Pattern.compile(pattern).matcher(actualValue.toString()).matches()

    companion object {
        @JvmField
        var ERROR_MARKER = "ERROR RETRIEVING VALUE: "

        private fun String.withinPeriod() = LocalDateTime.now().let {
            it.until(
                it.plus(parsePeriod(this.substring("!{within ".length, this.indexOf("}")).trim())),
                ChronoUnit.MILLIS
            )
        }

        fun Any?.isError() = this != null && toString().startsWith(ERROR_MARKER)
        fun Any?.isNotNull() = this != null && toString().startsWith("!{notNull}")
        fun Any?.isNumber() = this != null && toString().startsWith("!{number}")
        fun Any?.isString() = this != null && toString().startsWith("!{string}")
        fun Any?.isUuid() = this != null && toString().startsWith("!{uuid}")
        fun Any?.isRegex() = this != null && toString().startsWith("!{regex}")
        fun Any?.isWithin() = this != null && toString().startsWith("!{within ")
        fun Any?.isDbMatcher() =
            this is String && (isUuid() || isRegex() || isWithin() || isNumber() || isString() || isNotNull())
    }
}

class WithinValueComparer(tolerance: Long) : IsActualWithinToleranceOfExpectedTimestampValueComparer(0, tolerance) {
    public override fun isExpected(
        expectedTable: ITable?,
        actualTable: ITable?,
        rowNum: Int,
        columnName: String?,
        dataType: DataType,
        expectedValue: Any?,
        actualValue: Any?
    ) = super.isExpected(expectedTable, actualTable, rowNum, columnName, dataType, expectedValue, actualValue)

    override fun convertValueToTimeInMillis(timestampValue: Any?) =
        if (timestampValue is java.sql.Date) timestampValue.time
        else super.convertValueToTimeInMillis(timestampValue)
}

/**
 * Base class for default comparer overriding
 * @see IgnoreMillisComparer
 */
abstract class AbstractFallbackComparer : ExamMatchersAwareValueComparer() {
    override fun isExpected(
        expectedTable: ITable?,
        actualTable: ITable?,
        rowNum: Int,
        columnName: String?,
        dataType: DataType,
        expected: Any?,
        actual: Any?
    ): Boolean = if (super.isExpected(expectedTable, actualTable, rowNum, columnName, dataType, expected, actual)) true
    else compare(expected, actual)

    abstract fun compare(expected: Any?, actual: Any?): Boolean
}

class IgnoreMillisComparer : AbstractFallbackComparer() {
    override fun compare(expected: Any?, actual: Any?): Boolean {
        val expectedDt = (expected as Date).toLocalDateTime().withNano(0)
        val actualDt = (actual as Timestamp).toLocalDateTime()
        return expectedDt.isEqual(actualDt) || expectedDt.plusSeconds(1).isEqual(actualDt)
    }
}

fun sortedTable(table: ITable, columns: Array<String>, rowComparator: RowComparator) =
    SortedTable(table, columns).apply {
        setUseComparable(true)
        setRowComparator(rowComparator.init(table, columns))
    }
