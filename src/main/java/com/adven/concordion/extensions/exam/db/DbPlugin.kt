package com.adven.concordion.extensions.exam.db

import com.adven.concordion.extensions.exam.core.ExamPlugin
import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.html.TABLE
import com.adven.concordion.extensions.exam.core.html.span
import com.adven.concordion.extensions.exam.db.commands.*
import org.concordion.api.Element
import org.dbunit.assertion.DiffCollectingFailureHandler
import org.dbunit.assertion.comparer.value.ValueComparer
import org.dbunit.database.DatabaseConfig
import org.dbunit.dataset.SortedTable
import org.joda.time.format.DateTimeFormat
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class DbPlugin @JvmOverloads constructor(
    private val dbTester: DbTester,
    private val valuePrinter: ValuePrinter = ValuePrinter.Simple(),
    private val dbUnitConfig: DbUnitConfig = DbUnitConfig()
) : ExamPlugin {

    /***
     * @param dbUnitConfig properties for org.dbunit.database.DatabaseConfig
     */
    @JvmOverloads
    @Suppress("unused")
    constructor(
        driver: String,
        url: String,
        user: String,
        password: String,
        schema: String? = null,
        valuePrinter: ValuePrinter = ValuePrinter.Simple(),
        dbUnitConfig: DbUnitConfig = DbUnitConfig()
    ) : this(DbTester(driver, url, user, password, schema, dbUnitConfig.databaseConfigProperties), valuePrinter, dbUnitConfig)

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
        valuePrinter: ValuePrinter = ValuePrinter.Simple(),
        dbUnitConfig: DbUnitConfig = DbUnitConfig()
    ) : this(defaultTester, valuePrinter, dbUnitConfig) {
        for ((key, value) in others) {
            dbTester.executors[key] = value
        }
    }

    override fun commands(): List<ExamCommand> = listOf(
        DBShowCommand("db-show", TABLE, dbTester, valuePrinter),
        DBCheckCommand("db-check", TABLE, dbTester, valuePrinter, dbUnitConfig),
        DBSetCommand("db-set", TABLE, dbTester, valuePrinter),
        DBCleanCommand("db-clean", "span", dbTester)
    )

    interface ValuePrinter {
        open class Simple @JvmOverloads constructor(dateFormat: String = "yyyy-MM-dd HH:mm:ss.sss") : AbstractDefault(dateFormat) {
            override fun orElse(value: Any?): String = value.toString()
        }

        abstract class AbstractDefault @JvmOverloads constructor(private val dateFormat: String = "yyyy-MM-dd HH:mm:ss.sss") : ValuePrinter {
            override fun print(value: Any?): String = when (value) {
                value == null -> "(null)"
                is Array<*> -> value.contentToString()
                is Date -> DateTimeFormat.forPattern(dateFormat).print(value.time)
                else -> orElse(value)
            }

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
    val overrideRowSortingComparer: SortedTable.AbstractRowComparator? = null,
    val diffFailureHandler: DiffCollectingFailureHandler = DiffCollectingFailureHandler()
)