package io.github.adven27.concordion.extensions.exam.db

import io.github.adven27.concordion.extensions.exam.core.ExamPlugin
import io.github.adven27.concordion.extensions.exam.core.commands.ExamCommand
import io.github.adven27.concordion.extensions.exam.core.html.span
import io.github.adven27.concordion.extensions.exam.core.toDatePattern
import io.github.adven27.concordion.extensions.exam.db.builder.SeedStrategy
import io.github.adven27.concordion.extensions.exam.db.commands.DBCheckCommand
import io.github.adven27.concordion.extensions.exam.db.commands.DBCleanCommand
import io.github.adven27.concordion.extensions.exam.db.commands.DBSetCommand
import io.github.adven27.concordion.extensions.exam.db.commands.DBShowCommand
import io.github.adven27.concordion.extensions.exam.db.commands.DataSetExecuteCommand
import io.github.adven27.concordion.extensions.exam.db.commands.DataSetVerifyCommand
import io.github.adven27.concordion.extensions.exam.db.commands.RegexAndWithinAwareValueComparer
import org.concordion.api.Element
import org.dbunit.assertion.DiffCollectingFailureHandler
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
    val dbTester: DbTester,
    private val connectOnDemand: Boolean = true,
    private val valuePrinter: ValuePrinter = ValuePrinter.Simple(),
    private val allowedSeedStrategies: List<SeedStrategy> = SeedStrategy.values().toList(),
) : ExamPlugin {

    /**
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
        connectOnDemand: Boolean = true,
        valuePrinter: ValuePrinter = ValuePrinter.Simple(),
        dbUnitConfig: DbUnitConfig = DbUnitConfig(),
        allowedSeedStrategies: List<SeedStrategy> = SeedStrategy.values().toList(),
    ) : this(DbTester(driver, url, user, password, schema, dbUnitConfig), connectOnDemand, valuePrinter, allowedSeedStrategies)

    @JvmOverloads
    @Suppress("unused")
    constructor(dbTester: DbTester, allowedSeedStrategies: List<SeedStrategy>, connectOnDemand: Boolean = true) : this(
        dbTester,
        connectOnDemand,
        ValuePrinter.Simple(),
        allowedSeedStrategies
    )

    init {
        dbTester.executors[DbTester.DEFAULT_DATASOURCE] = dbTester
    }

    /**
     * @param defaultTester Default datasource, used when `ds` attribute is omitted: `<e:db-set ...>`
     * @param others Map of additional datasources, used when `ds` attribute present: `<e:db-set ds="other" ...>`
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
        connectOnDemand: Boolean = true,
        valuePrinter: ValuePrinter = ValuePrinter.Simple(),
        allowedSeedStrategies: List<SeedStrategy> = SeedStrategy.values().toList(),
    ) : this(defaultTester, connectOnDemand, valuePrinter, allowedSeedStrategies) {
        for ((key, value) in others) {
            dbTester.executors[key] = value
        }
    }

    @JvmOverloads
    @Suppress("unused")
    constructor(
        defaultTester: DbTester,
        others: Map<String, DbTester>,
        allowedSeedStrategies: List<SeedStrategy>,
        connectOnDemand: Boolean = true
    ) : this(defaultTester, others, connectOnDemand, ValuePrinter.Simple(), allowedSeedStrategies)

    override fun commands(): List<ExamCommand> = listOf(
        DataSetExecuteCommand("db-execute", "span", dbTester, valuePrinter, allowedSeedStrategies),
        DataSetVerifyCommand("db-verify", "span", dbTester, valuePrinter),
        DBShowCommand("db-show", "div", dbTester, valuePrinter),
        DBCheckCommand("db-check", "div", dbTester, valuePrinter),
        DBSetCommand("db-set", "div", dbTester, valuePrinter, allowedSeedStrategies),
        DBCleanCommand("db-clean", "pre", dbTester)
    )

    override fun setUp() {
        if (!connectOnDemand) dbTester.connection
    }

    override fun tearDown() {
        dbTester.close()
    }

    /**
     * Defines how to print and render values in '<e:db-*' commands
     * @see JsonValuePrinter
     */
    interface ValuePrinter {
        open class Simple @JvmOverloads constructor(dateFormat: String = "yyyy-MM-dd HH:mm:ss.SSS") :
            AbstractDefault(dateFormat) {
            override fun orElse(value: Any): String = value.toString()
        }

        abstract class AbstractDefault @JvmOverloads constructor(private val dateFormat: String = "yyyy-MM-dd HH:mm:ss.SSS") :
            ValuePrinter {
            override fun print(value: Any?): String = when (value) {
                null -> "(null)"
                is Array<*> -> value.contentToString()
                is java.sql.Date -> printDate(Date(value.time))
                is Date -> printDate(value)
                else -> orElse(value)
            }

            private fun printDate(value: Date) =
                dateFormat.toDatePattern().withZone(ZoneId.systemDefault()).format(value.toInstant())

            override fun wrap(value: Any?): Element = span(print(value)).el

            abstract fun orElse(value: Any): String
        }

        fun print(value: Any?): String
        fun wrap(value: Any?): Element
    }

    open class JsonValuePrinter : ValuePrinter.Simple() {
        override fun wrap(value: Any?): Element =
            if (isJson(value)) Element("pre").addStyleClass("json").appendText(print(value)) else super.wrap(value)

        protected fun isJson(value: Any?): Boolean = value is String && value.startsWith("{") && value.endsWith("}")
    }
}

data class DbUnitConfig @JvmOverloads constructor(
    val databaseConfigProperties: Map<String, Any?> = mapOf(),
    val valueComparer: RegexAndWithinAwareValueComparer = RegexAndWithinAwareValueComparer(),
    val columnValueComparers: Map<String, RegexAndWithinAwareValueComparer> = emptyMap(),
    val overrideRowSortingComparer: RowComparator = RowComparator(),
    val diffFailureHandler: DiffCollectingFailureHandler = DiffCollectingFailureHandler(),
    val isColumnSensing: Boolean = false
) {
    @Suppress("unused")
    class Builder {
        var databaseConfigProperties: Map<String, Any?> = mapOf()
        var valueComparer: RegexAndWithinAwareValueComparer = RegexAndWithinAwareValueComparer()
        var columnValueComparers: Map<String, RegexAndWithinAwareValueComparer> = emptyMap()
        var overrideRowSortingComparer: RowComparator = RowComparator()
        var diffFailureHandler: DiffCollectingFailureHandler = DiffCollectingFailureHandler()
        var columnSensing: Boolean = false

        fun databaseConfigProperties(databaseConfigProperties: Map<String, Any?>) =
            apply { this.databaseConfigProperties += databaseConfigProperties }

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
