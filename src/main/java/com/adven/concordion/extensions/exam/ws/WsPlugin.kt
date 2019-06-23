package com.adven.concordion.extensions.exam.ws

import com.adven.concordion.extensions.exam.core.ExamExtension
import com.adven.concordion.extensions.exam.core.ExamPlugin
import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import io.restassured.RestAssured
import net.javacrumbs.jsonunit.core.Configuration
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.ElementSelectors.byName
import org.xmlunit.diff.ElementSelectors.byNameAndText
import org.xmlunit.diff.NodeMatcher

//@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
class WsPlugin constructor(
    uri: String = "http://localhost", basePath: String = "", port: Int = 8080,
    private val nodeMatcher: NodeMatcher = DefaultNodeMatcher(byNameAndText, byName),
    private val jsonUnitCfg: Configuration = ExamExtension.DEFAULT_JSON_UNIT_CFG
) : ExamPlugin {

    constructor(withPort: Int) : this(port = withPort)
    constructor(withUri: String) : this(uri = withUri)
    constructor(withBasePath: String, withPort: Int) : this(basePath = withBasePath, port = withPort)
    constructor(withUri: String, withBasePath: String) : this(uri = withUri, basePath = withBasePath)
    constructor(withUri: String, withBasePath: String, withPort: Int) :
            this(uri = withUri, basePath = withBasePath, port = withPort)

    constructor(withUri: String, withBasePath: String, withPort: Int, withNodeMatcher: NodeMatcher) :
            this(uri = withUri, basePath = withBasePath, port = withPort, nodeMatcher = withNodeMatcher)

    constructor(withUri: String, withBasePath: String, withPort: Int, withJsonUnitCfg: Configuration) :
            this(uri = withUri, basePath = withBasePath, port = withPort, jsonUnitCfg = withJsonUnitCfg)

    constructor(withBasePath: String, withPort: Int, withNodeMatcher: NodeMatcher) :
            this(basePath = withBasePath, port = withPort, nodeMatcher = withNodeMatcher)

    constructor(withBasePath: String, withPort: Int, withJsonUnitCfg: Configuration) :
            this(basePath = withBasePath, port = withPort, jsonUnitCfg = withJsonUnitCfg)

    constructor(
        withBasePath: String, withPort: Int, withNodeMatcher: NodeMatcher, withJsonUnitCfg: Configuration
    ) : this(
        basePath = withBasePath, port = withPort, nodeMatcher = withNodeMatcher, jsonUnitCfg = withJsonUnitCfg
    )

    constructor(withNodeMatcher: NodeMatcher) : this(nodeMatcher = withNodeMatcher)
    constructor(withJsonUnitCfg: Configuration) : this(jsonUnitCfg = withJsonUnitCfg)
    constructor(withNodeMatcher: NodeMatcher, withJsonUnitCfg: Configuration) :
            this(nodeMatcher = withNodeMatcher, jsonUnitCfg = withJsonUnitCfg)

    init {
        RestAssured.baseURI = uri
        RestAssured.basePath = basePath
        RestAssured.port = port
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