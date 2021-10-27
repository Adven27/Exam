package specs.md

import org.concordion.api.option.ConcordionOptions
import org.concordion.api.option.MarkdownExtensions.AUTOLINKS
import org.concordion.api.option.MarkdownExtensions.DEFINITIONS
import org.concordion.api.option.MarkdownExtensions.FENCED_CODE_BLOCKS
import org.concordion.api.option.MarkdownExtensions.FORCELISTITEMPARA
import org.concordion.api.option.MarkdownExtensions.TASKLISTITEMS
import org.concordion.api.option.MarkdownExtensions.WIKILINKS
import specs.Specs

@ConcordionOptions(
    markdownExtensions = [WIKILINKS, AUTOLINKS, FENCED_CODE_BLOCKS, DEFINITIONS, FORCELISTITEMPARA, TASKLISTITEMS]
)
class Markdown : Specs()
