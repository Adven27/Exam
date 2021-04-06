package io.github.adven27.concordion.extensions.exam.files

import io.github.adven27.concordion.extensions.exam.core.ExamExtension
import io.github.adven27.concordion.extensions.exam.core.ExamPlugin
import io.github.adven27.concordion.extensions.exam.core.commands.ExamCommand
import io.github.adven27.concordion.extensions.exam.core.html.TABLE
import io.github.adven27.concordion.extensions.exam.files.commands.FilesCheckCommand
import io.github.adven27.concordion.extensions.exam.files.commands.FilesSetCommand
import io.github.adven27.concordion.extensions.exam.files.commands.FilesShowCommand
import net.javacrumbs.jsonunit.core.Configuration
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.ElementSelectors.byName
import org.xmlunit.diff.ElementSelectors.byNameAndText
import org.xmlunit.diff.NodeMatcher

class FlPlugin @JvmOverloads constructor(
    private var filesLoader: FilesLoader = DefaultFilesLoader(),
    private var jsonUnitCfg: Configuration = ExamExtension.DEFAULT_JSON_UNIT_CFG,
    private var nodeMatcher: NodeMatcher = DefaultNodeMatcher(byNameAndText, byName)
) : ExamPlugin.NoSetUp() {
    override fun commands(): List<ExamCommand> = listOf(
        FilesShowCommand("fl-show", TABLE, filesLoader),
        FilesSetCommand("fl-set", TABLE, filesLoader),
        FilesCheckCommand("fl-check", TABLE, jsonUnitCfg, nodeMatcher, filesLoader)
    )
}
