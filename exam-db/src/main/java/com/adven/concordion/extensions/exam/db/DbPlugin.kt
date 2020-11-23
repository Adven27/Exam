package com.adven.concordion.extensions.exam.db

import com.adven.concordion.extensions.exam.core.ExamPlugin
import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.html.TABLE
import com.adven.concordion.extensions.exam.core.html.span
import com.adven.concordion.extensions.exam.core.toDatePattern
import com.adven.concordion.extensions.exam.db.commands.DBCheckCommand
import com.adven.concordion.extensions.exam.db.commands.DBCleanCommand
import com.adven.concordion.extensions.exam.db.commands.DBSetCommand
import com.adven.concordion.extensions.exam.db.commands.DBShowCommand
import com.adven.concordion.extensions.exam.db.commands.DataSetExecuteCommand
import com.adven.concordion.extensions.exam.db.commands.DataSetVerifyCommand
import com.adven.concordion.extensions.exam.db.commands.RegexAndWithinAwareValueComparer
import org.concordion.api.Element
import org.dbunit.assertion.DiffCollectingFailureHandler
import org.dbunit.database.DatabaseConfig
import org.dbunit.dataset.Column
import org.dbunit.dataset.Columns.findColumnsByName
import org.dbunit.dataset.ITable
import org.dbunit.dataset.SortedTable
import java.time.ZoneId
import java.util.Date
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class DbPlugin @JvmOverloads constructor(
    private val dbTester: DbTester,
    private val valuePrinter: ValuePrinter = ValuePrinter.Simple()
) : ExamPlugin {

    /***
     * @param dbUnitConfig properties for org.dbunit.database.DatabaseConfig
     */
    @JvmOverloads
    @Suppress("unused", "LongParameterList")
    constructor(
        driver: String,
        url: String,
        user: String,
        password: String,
        schema: String? = null,
        valuePrinter: ValuePrinter = ValuePrinter.Simple(),
        dbUnitConfig: DbUnitConfig = DbUnitConfig()
    ) : this(DbTester(driver, url, user, password, schema, dbUnitConfig), valuePrinter)

    init {
        dbTester.executors[DbTester.DEFAULT_DATASOURCE] = dbTester
    }

    /**
     * @param defaultTester Default datasource: used when `ds` attribute is omitted, for example `<e:db-set>`
     * @param others map of additional datasources: used when `ds` attribute present, for example `<e:db-set ds="other"`
     *
     *```
     * DbPlugin(
     *     DbTester(...),
     *     mapOf("other" to DbTester(...)),
     *  ...
     * )
     * ```
     */
    @Suppress("unused")
    @JvmOverloads
    constructor(
        defaultTester: DbTester,
        others: Map<String, DbTester>,
        valuePrinter: ValuePrinter = ValuePrinter.Simple()
    ) : this(defaultTester, valuePrinter) {
        for ((key, value) in others) {
            dbTester.executors[key] = value
        }
    }

    override fun commands(): List<ExamCommand> = listOf(
        DataSetExecuteCommand("db-execute", "span", dbTester, valuePrinter),
        DataSetVerifyCommand("db-verify", "span", dbTester, valuePrinter),
        DBShowCommand("db-show", TABLE, dbTester, valuePrinter),
        DBCheckCommand("db-check", TABLE, dbTester, valuePrinter),
        DBSetCommand("db-set", TABLE, dbTester, valuePrinter),
        DBCleanCommand("db-clean", "span", dbTester)
    )

    interface ValuePrinter {
        open class Simple @JvmOverloads constructor(dateFormat: String = "yyyy-MM-dd HH:mm:ss.SSS") :
            AbstractDefault(dateFormat) {
            override fun orElse(value: Any?): String = value.toString()
        }

        abstract class AbstractDefault @JvmOverloads constructor(private val dateFormat: String = "yyyy-MM-dd HH:mm:ss.SSS") :
            ValuePrinter {
            override fun print(value: Any?): String = when (value) {
                value == null -> "(null)"
                is Array<*> -> value.contentToString()
                is Date -> printDate(value)
                else -> orElse(value)
            }

            private fun printDate(value: Date) =
                dateFormat.toDatePattern().withZone(ZoneId.systemDefault()).format(value.toInstant())

            override fun wrap(value: Any?): Element = span(print(value)).el

            abstract fun orElse(value: Any?): String
        }

        fun print(value: Any?): String
        fun wrap(value: Any?): Element
    }
}

data class DbUnitConfig @JvmOverloads constructor(
    val databaseConfigProperties: Map<String, Any?> = mapOf(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS to true),
    val valueComparer: RegexAndWithinAwareValueComparer = RegexAndWithinAwareValueComparer(),
    val columnValueComparers: Map<String, RegexAndWithinAwareValueComparer> = emptyMap(),
    val overrideRowSortingComparer: RowComparator = RowComparator(),
    val diffFailureHandler: DiffCollectingFailureHandler = DiffCollectingFailureHandler(),
    val isColumnSensing: Boolean = false
) {
    class Builder {
        var databaseConfigProperties: Map<String, Any?> = mapOf(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS to true)
        var valueComparer: RegexAndWithinAwareValueComparer = RegexAndWithinAwareValueComparer()
        var columnValueComparers: Map<String, RegexAndWithinAwareValueComparer> = emptyMap()
        var overrideRowSortingComparer: RowComparator = RowComparator()
        var diffFailureHandler: DiffCollectingFailureHandler = DiffCollectingFailureHandler()
        var columnSensing: Boolean = false

        fun databaseConfigProperties(databaseConfigProperties: Map<String, Any?>) =
            apply { this.databaseConfigProperties = databaseConfigProperties }

        fun valueComparer(valueComparer: RegexAndWithinAwareValueComparer) =
            apply { this.valueComparer = valueComparer }

        fun columnValueComparers(columnValueComparers: Map<String, RegexAndWithinAwareValueComparer>) =
            apply { this.columnValueComparers = columnValueComparers }

        fun overrideRowSortingComparer(overrideRowSortingComparer: RowComparator = RowComparator()) =
            apply { this.overrideRowSortingComparer = overrideRowSortingComparer }

        fun diffFailureHandler(diffFailureHandler: DiffCollectingFailureHandler) =
            apply { this.diffFailureHandler = diffFailureHandler }

        fun columnSensing(columnSensing: Boolean) = apply { this.columnSensing = columnSensing }
        fun build() = DbUnitConfig(
            databaseConfigProperties,
            valueComparer,
            columnValueComparers,
            overrideRowSortingComparer,
            diffFailureHandler,
            columnSensing
        )
    }

    fun isCaseSensitiveTableNames() = databaseConfigProperties.containsKey("caseSensitiveTableNames") &&
        databaseConfigProperties["caseSensitiveTableNames"].toString().toBoolean()
}

open class RowComparator {
    fun init(table: ITable, sortCols: Array<String>) =
        object : SortedTable.AbstractRowComparator(table, findColumnsByName(sortCols, table.tableMetaData)) {
            override fun compare(col: Column?, val1: Any?, val2: Any?) = this@RowComparator.compare(col, val1, val2)
        }

    open fun compare(column: Column?, value1: Any?, value2: Any?): Int = try {
        column!!.dataType.compare(value1, value2)
    } catch (ignore: Exception) {
        0
    }
}
