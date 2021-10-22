package io.github.adven27.concordion.extensions.exam.db.commands

import io.github.adven27.concordion.extensions.exam.core.commands.await
import io.github.adven27.concordion.extensions.exam.core.commands.awaitConfig
import io.github.adven27.concordion.extensions.exam.core.commands.timeoutMessage
import io.github.adven27.concordion.extensions.exam.core.errorMessage
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.div
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.core.html.pre
import io.github.adven27.concordion.extensions.exam.core.html.span
import io.github.adven27.concordion.extensions.exam.core.resolveToObj
import io.github.adven27.concordion.extensions.exam.core.toLocalDateTime
import io.github.adven27.concordion.extensions.exam.core.utils.parsePeriod
import io.github.adven27.concordion.extensions.exam.db.DbPlugin
import io.github.adven27.concordion.extensions.exam.db.DbResultRenderer
import io.github.adven27.concordion.extensions.exam.db.DbTester
import io.github.adven27.concordion.extensions.exam.db.RowComparator
import org.awaitility.core.ConditionTimeoutException
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.Result.FAILURE
import org.concordion.api.Result.SUCCESS
import org.concordion.api.ResultRecorder
import org.concordion.api.listener.AssertEqualsListener
import org.concordion.api.listener.AssertFailureEvent
import org.concordion.api.listener.AssertSuccessEvent
import org.concordion.internal.util.Announcer
import org.dbunit.Assertion
import org.dbunit.assertion.DbComparisonFailure
import org.dbunit.assertion.Difference
import org.dbunit.assertion.comparer.value.IsActualEqualToExpectedValueComparer
import org.dbunit.assertion.comparer.value.IsActualWithinToleranceOfExpectedTimestampValueComparer
import org.dbunit.dataset.CompositeTable
import org.dbunit.dataset.ITable
import org.dbunit.dataset.SortedTable
import org.dbunit.dataset.datatype.DataType
import org.dbunit.util.QualifiedTableName
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID
import java.util.regex.Pattern

@Suppress("TooManyFunctions")
class DBCheckCommand(
    name: String,
    tag: String,
    dbTester: DbTester,
    valuePrinter: DbPlugin.ValuePrinter
) : DBCommand(name, tag, dbTester, valuePrinter) {
    private val listeners = Announcer.to(AssertEqualsListener::class.java)

    private val actualTable: ITable
        get() {
            val conn = dbTester.connectionFor(ds)
            val qualifiedName = QualifiedTableName(expectedTable.tableName(), conn.schema).qualifiedName
            val where = if (where.isNullOrEmpty()) "" else "WHERE $where"
            return conn.createQueryTable(qualifiedName, "select * from $qualifiedName $where")
        }

    init {
        listeners.addListener(DbResultRenderer())
    }

    private fun failure(resultRecorder: ResultRecorder, html: Html, actual: Any?, expected: String): Html {
        resultRecorder.record(FAILURE)
        listeners.announce().failureReported(AssertFailureEvent(html.el, expected, actual))
        return html
    }

    private fun success(resultRecorder: ResultRecorder, html: Html): Html {
        resultRecorder.record(SUCCESS)
        listeners.announce().successReported(AssertSuccessEvent(html.el))
        return html
    }

    override fun verify(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) {
        dbTester.dbUnitConfig.valueComparer.setEvaluator(evaluator!!)
        dbTester.dbUnitConfig.columnValueComparers.forEach { it.value.setEvaluator(evaluator) }
        assertEq(cmd.html().apply { attr("class", attr("class") + " db-check") }, resultRecorder)
    }

    private fun assertEq(rootEl: Html, resultRecorder: ResultRecorder?) {
        var root = rootEl
        val sortCols: Array<String> = if (orderBy.isEmpty()) expectedTable.columnNamesArray() else orderBy
        var actual = sortedTable(
            actualTable.withColumnsAsIn(expectedTable),
            sortCols,
            dbTester.dbUnitConfig.overrideRowSortingComparer
        )
        val expected = sortedTable(
            CompositeTable(actual.tableMetaData, expectedTable),
            sortCols,
            dbTester.dbUnitConfig.overrideRowSortingComparer
        )
        val awaitConfig = root.awaitConfig()
        try {
            if (awaitConfig.enabled()) {
                try {
                    awaitConfig.await("Await DB table ${expected.tableName()}").untilAsserted {
                        actual = sortedTable(
                            actualTable.withColumnsAsIn(expectedTable),
                            sortCols,
                            dbTester.dbUnitConfig.overrideRowSortingComparer
                        )
                        dbUnitAssert(expected, actual)
                        throwIfDiffs()
                    }
                } catch (f: ConditionTimeoutException) {
                    root(pre(awaitConfig.timeoutMessage(f)).css("alert alert-danger small"))
                    throwIfDbComparisonFailure(f)
                }
            } else {
                dbUnitAssert(expected, actual)
            }
        } catch (f: DbComparisonFailure) {
            root = rowsCountMismatch(resultRecorder, f, root, actual)
        } finally {
            checkResult(
                root,
                expected,
                actual,
                dbTester.dbUnitConfig.diffFailureHandler.diffList as List<Difference>,
                resultRecorder!!
            )
        }
    }

    private fun throwIfDbComparisonFailure(f: ConditionTimeoutException) {
        if (f.cause is DbComparisonFailure) {
            throw f.cause as DbComparisonFailure
        }
    }

    private fun throwIfDiffs() {
        if (dbTester.dbUnitConfig.diffFailureHandler.diffList.isNotEmpty()) {
            throw AssertionError()
        }
    }

    // TODO move to ResultRenderer
    private fun rowsCountMismatch(
        resultRecorder: ResultRecorder?,
        f: DbComparisonFailure,
        root: Html,
        actual: SortedTable
    ): Html {
        resultRecorder!!.record(FAILURE)
        val exp = div()
        val (_, errorMsg) = errorMessage(
            message = f.message ?: "",
            html = div().css("")(
                span("Expected: "),
                exp,
                span("but was: "),
                renderTable(null, actual, remarks, valuePrinter)
            ),
            type = "text"
        )
        root(errorMsg)
        return exp
    }

    private fun dbUnitAssert(expected: SortedTable, actual: ITable) {
        dbTester.dbUnitConfig.diffFailureHandler.diffList.clear()
        Assertion.assertWithValueComparer(
            expected,
            actual,
            dbTester.dbUnitConfig.diffFailureHandler,
            dbTester.dbUnitConfig.valueComparer,
            dbTester.dbUnitConfig.columnValueComparers
        )
    }

    private fun checkResult(
        root: Html,
        expected: ITable,
        actual: ITable,
        diffs: List<Difference>,
        resultRecorder: ResultRecorder
    ) {
        val markAsSuccessOrFailure: (Html, Int, String) -> Html = { td, row, col ->
            val value = expected[row, col]
            val expectedValue = valuePrinter.wrap(value)
            diffs.firstOrNull { diff ->
                diff.rowIndex == row && diff.columnName == col
            }?.markAsFailure(resultRecorder, td)
                ?: td.markAsSuccess(resultRecorder)(
                    Html(expectedValue).text(
                        appendIf(value.isDbMatcher() && actual.rowCount == expected.rowCount, actual, row, col)
                    )
                )
        }
        root(
            renderTable(
                root.takeAwayAttr("caption"),
                expected,
                markAsSuccessOrFailure,
                ifEmpty = { markAsSuccess(resultRecorder) }
            )
        )
    }

    private fun appendIf(append: Boolean, actual: ITable, row: Int, col: String): String =
        if (append) " (${actual[row, col]})" else ""

    private fun Html.markAsSuccess(resultRecorder: ResultRecorder) = success(resultRecorder, this)
    private fun Difference.markAsFailure(resultRecorder: ResultRecorder, td: Html): Html {
        return failure(resultRecorder, td, valuePrinter.print(this.actualValue), valuePrinter.print(this.expectedValue))
    }

    companion object {
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

open class RegexAndWithinAwareValueComparer : IsActualEqualToExpectedValueComparer() {
    protected lateinit var evaluator: Evaluator

    fun setEvaluator(evaluator: Evaluator): RegexAndWithinAwareValueComparer {
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
        expected.isNotNull() -> setVarIfNeeded(actual, expected) { a, _ -> a != null }
        expected.isNumber() -> setVarIfNeeded(actual, expected) { a, _ -> regexMatches("^\\d+\$", a) }
        expected.isString() -> setVarIfNeeded(actual, expected) { a, _ -> regexMatches("^\\w+\$", a) }
        expected.isUuid() -> setVarIfNeeded(actual, expected) { a, _ -> isUuid(a) }
        expected.isRegex() -> setVarIfNeeded(actual, expected) { a, e -> regexMatches(e, a) }
        expected.isWithin() -> setVarIfNeeded(actual, expected) { a, e ->
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

    private fun setVarIfNeeded(
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
}

/**
 * Base class for default comparer overriding
 * @see IgnoreMillisComparer
 */
abstract class AbstractFallbackComparer : RegexAndWithinAwareValueComparer() {
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

private fun Any?.isNotNull() = this != null && this.toString().startsWith("!{notNull}")
private fun Any?.isNumber() = this != null && this.toString().startsWith("!{number}")
private fun Any?.isString() = this != null && this.toString().startsWith("!{string}")
private fun Any?.isUuid() = this != null && this.toString().startsWith("!{uuid}")
private fun Any?.isRegex() = this != null && this.toString().startsWith("!{regex}")
private fun Any?.isWithin() = this != null && this.toString().startsWith("!{within ")

private fun String.withinPeriod() = LocalDateTime.now().let {
    it.until(
        it.plus(parsePeriod(this.substring("!{within ".length, this.indexOf("}")).trim())),
        ChronoUnit.MILLIS
    )
}

fun sortedTable(table: ITable, columns: Array<String>, rowComparator: RowComparator) =
    SortedTable(table, columns).apply {
        setUseComparable(true)
        setRowComparator(rowComparator.init(table, columns))
    }
