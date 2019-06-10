package com.adven.concordion.extensions.exam;

import com.adven.concordion.extensions.exam.commands.ExamBeforeExampleCommand;
import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.commands.ExamExampleCommand;
import com.adven.concordion.extensions.exam.commands.ExamplesSummaryCommand;
import com.adven.concordion.extensions.exam.commands.GivenCommand;
import com.adven.concordion.extensions.exam.commands.InlineBeforeExampleCommand;
import com.adven.concordion.extensions.exam.commands.JsonCheckCommand;
import com.adven.concordion.extensions.exam.commands.ScrollToTopCommand;
import com.adven.concordion.extensions.exam.commands.SetVarCommand;
import com.adven.concordion.extensions.exam.commands.ThenCommand;
import com.adven.concordion.extensions.exam.commands.WhenCommand;
import com.adven.concordion.extensions.exam.commands.XmlCheckCommand;
import com.adven.concordion.extensions.exam.configurators.ExamDbTester;
import com.adven.concordion.extensions.exam.db.commands.DBCheckCommand;
import com.adven.concordion.extensions.exam.db.commands.DBSetCommand;
import com.adven.concordion.extensions.exam.db.commands.DBShowCommand;
import com.adven.concordion.extensions.exam.files.FilesLoader;
import com.adven.concordion.extensions.exam.files.commands.FilesCheckCommand;
import com.adven.concordion.extensions.exam.files.commands.FilesSetCommand;
import com.adven.concordion.extensions.exam.files.commands.FilesShowCommand;
import com.adven.concordion.extensions.exam.mq.MqCheckCommand;
import com.adven.concordion.extensions.exam.mq.MqSendCommand;
import com.adven.concordion.extensions.exam.mq.MqTester;
import com.adven.concordion.extensions.exam.rest.CaseCheckCommand;
import com.adven.concordion.extensions.exam.rest.CaseCommand;
import com.adven.concordion.extensions.exam.rest.DeleteCommand;
import com.adven.concordion.extensions.exam.rest.ExpectedStatusCommand;
import com.adven.concordion.extensions.exam.rest.GetCommand;
import com.adven.concordion.extensions.exam.rest.PostCommand;
import com.adven.concordion.extensions.exam.rest.PutCommand;
import com.adven.concordion.extensions.exam.rest.SoapCommand;
import com.adven.concordion.extensions.exam.ui.BrowserCommand;
import net.javacrumbs.jsonunit.core.Configuration;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.xmlunit.diff.NodeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class CommandRegistry {
    private static final String TABLE = "table";

    private final List<ExamCommand> commands = new ArrayList<>();

    public CommandRegistry(
        ExamDbTester dbTester,
        Configuration jsonUnitCfg,
        NodeMatcher nodeMatcher,
        DesiredCapabilities capabilities,
        FilesLoader filesLoader,
        Map<String, MqTester> mqTesters) {

        commands.addAll(asList(
            new GivenCommand("div"),
            new WhenCommand("div"),
            new ThenCommand("div"),

            new ExamExampleCommand("div"),
            new ExamBeforeExampleCommand("div"),
            new InlineBeforeExampleCommand("div"),
            new ExamplesSummaryCommand("summary", "div"),
            new ScrollToTopCommand("scrollToTop", "div"),

            new CaseCheckCommand("check", "div"),

            new FilesShowCommand("fl-show", TABLE, filesLoader),
            new FilesSetCommand("fl-set", TABLE, filesLoader),
            new FilesCheckCommand("fl-check", TABLE, jsonUnitCfg, nodeMatcher, filesLoader),

            new PostCommand("post", "div"),
            new SoapCommand("soap", "div"),
            new GetCommand("get", "div"),
            new PutCommand("put", "div"),
            new DeleteCommand("delete", "div"),
            new CaseCommand("tr", jsonUnitCfg, nodeMatcher),
            new ExpectedStatusCommand("rs-status", "code"),

            new BrowserCommand("div", capabilities),

            new SetVarCommand("span"),

            new MqCheckCommand("mq-check", "div", jsonUnitCfg, mqTesters),
            new MqSendCommand("mq-send", "div", mqTesters),
            new JsonCheckCommand("json-check", "div", jsonUnitCfg),
            new XmlCheckCommand("xml-check", "div", jsonUnitCfg, nodeMatcher)

        ));
        if (pluggedDbModule(dbTester)) {
            commands.addAll(asList(
                new DBShowCommand("db-show", TABLE, dbTester),
                new DBCheckCommand("db-check", TABLE, dbTester),
                new DBSetCommand("db-set", TABLE, dbTester))
            );
        }
    }

    private boolean pluggedDbModule(ExamDbTester dbTester) {
        return dbTester != null;
    }

    public ExamCommand getBy(String name) {
        for (ExamCommand cmd : commands) {
            if (cmd.name().equals(name)) {
                return cmd;
            }
        }
        return null;
    }

    public List<ExamCommand> commands() {
        return new ArrayList<>(commands);
    }
}