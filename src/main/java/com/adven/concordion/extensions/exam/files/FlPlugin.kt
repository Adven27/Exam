package com.adven.concordion.extensions.exam.files

import com.adven.concordion.extensions.exam.core.ExamPlugin
import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.files.commands.FilesCheckCommand
import com.adven.concordion.extensions.exam.files.commands.FilesSetCommand
import com.adven.concordion.extensions.exam.files.commands.FilesShowCommand
import com.adven.concordion.extensions.exam.core.utils.DateFormatMatcher
import com.adven.concordion.extensions.exam.core.utils.DateWithin
import com.adven.concordion.extensions.exam.core.utils.XMLDateWithin
import net.javacrumbs.jsonunit.JsonAssert.`when`
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.ElementSelectors.byName
import org.xmlunit.diff.ElementSelectors.byNameAndText
import org.xmlunit.diff.NodeMatcher

class FlPlugin @JvmOverloads constructor(
    private var filesLoader: FilesLoader = DefaultFilesLoader(),
    private var jsonUnitCfg: Configuration = `when`(Option.IGNORING_ARRAY_ORDER)
        .withMatcher("formattedAs", DateFormatMatcher())
        .withMatcher("formattedAndWithin", DateWithin.param())
        .withMatcher("formattedAndWithinNow", DateWithin.now())
        .withMatcher("xmlDateWithinNow", XMLDateWithin()),
    private var nodeMatcher: NodeMatcher = DefaultNodeMatcher(byNameAndText, byName)
) : ExamPlugin {
    override fun commands(): List<ExamCommand> = listOf(
        FilesShowCommand("fl-show", TABLE, filesLoader),
        FilesSetCommand("fl-set", TABLE, filesLoader),
        FilesCheckCommand("fl-check", TABLE, jsonUnitCfg, nodeMatcher, filesLoader)
    )

    companion object {
        private const val TABLE = "table"
    }
}