package com.adven.concordion.extensions.exam.ws

import com.adven.concordion.extensions.exam.core.ExamPlugin
import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.utils.DateFormatMatcher
import com.adven.concordion.extensions.exam.core.utils.DateWithin
import com.adven.concordion.extensions.exam.core.utils.XMLDateWithin
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import net.javacrumbs.jsonunit.JsonAssert.`when`
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.ElementSelectors.byName
import org.xmlunit.diff.ElementSelectors.byNameAndText
import org.xmlunit.diff.NodeMatcher

@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
class WsPlugin @JvmOverloads constructor(
    private val nodeMatcher: NodeMatcher = DefaultNodeMatcher(byNameAndText, byName),
    private val jsonUnitCfg: Configuration = `when`(Option.IGNORING_ARRAY_ORDER)
        .withMatcher("formattedAs", DateFormatMatcher())
        .withMatcher("formattedAndWithin", DateWithin.param())
        .withMatcher("formattedAndWithinNow", DateWithin.now())
        .withMatcher("xmlDateWithinNow", XMLDateWithin()),
    init: () -> Unit = {}
) : ExamPlugin {

    init {
        init()
    }

    override fun commands(): List<ExamCommand> = listOf(
        SoapCommand("soap", "div"),
        PostCommand("post", "div"),
        GetCommand("get", "div"),
        PutCommand("put", "div"),
        DeleteCommand("delete", "div"),
        CaseCommand("tr", jsonUnitCfg, nodeMatcher),
        CaseCheckCommand("check", "div"),
        ExpectedStatusCommand("rs-status", "code")
    )
}