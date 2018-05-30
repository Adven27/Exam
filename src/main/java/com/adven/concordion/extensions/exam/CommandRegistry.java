package com.adven.concordion.extensions.exam;

import com.adven.concordion.extensions.exam.commands.*;
import com.adven.concordion.extensions.exam.db.commands.DBCheckCommand;
import com.adven.concordion.extensions.exam.db.commands.DBSetCommand;
import com.adven.concordion.extensions.exam.db.commands.DBShowCommand;
import com.adven.concordion.extensions.exam.db.kv.KVCheckCommand;
import com.adven.concordion.extensions.exam.db.kv.KeyValueRepository;
import com.adven.concordion.extensions.exam.files.FilesLoader;
import com.adven.concordion.extensions.exam.files.commands.FilesCheckCommand;
import com.adven.concordion.extensions.exam.files.commands.FilesSetCommand;
import com.adven.concordion.extensions.exam.files.commands.FilesShowCommand;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import com.adven.concordion.extensions.exam.kafka.commands.EventCheckReplyCommand;
import com.adven.concordion.extensions.exam.kafka.commands.EventSendCommand;
import com.adven.concordion.extensions.exam.rest.commands.*;
import com.adven.concordion.extensions.exam.ui.BrowserCommand;
import net.javacrumbs.jsonunit.core.Configuration;
import org.dbunit.IDatabaseTester;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.xmlunit.diff.NodeMatcher;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class CommandRegistry {

    private static final String TABLE = "table";

    private final List<ExamCommand> commands;

    public CommandRegistry(
            IDatabaseTester dbTester,
            Configuration jsonUnitCfg,
            NodeMatcher nodeMatcher,
            DesiredCapabilities capabilities,
            FilesLoader filesLoader,
            EventProcessor eventProcessor,
            KeyValueRepository keyValueRepository) {
        commands = asList(
                new GivenCommand("div"),
                new WhenCommand("div"),
                new ThenCommand("div"),

                new ExamExampleCommand("div"),
                new ExamBeforeExampleCommand("div"),
                new InlineBeforeExampleCommand("div"),
                new ExamplesSummaryCommand("summary", "div"),
                new ScrollToTopCommand("scrollToTop", "div"),

                new CaseCheckCommand("check", "div"),

                new DBShowCommand("db-show", TABLE, dbTester),
                new DBCheckCommand("db-check", TABLE, dbTester),
                new DBSetCommand("db-set", TABLE, dbTester),

                new FilesShowCommand("fl-show", TABLE, filesLoader),
                new FilesSetCommand("fl-set", TABLE, filesLoader),
                new FilesCheckCommand("fl-check", TABLE, jsonUnitCfg, nodeMatcher, filesLoader),

                new PostCommand("post", "div"),
                new GetCommand("get", "div"),
                new PutCommand("put", "div"),
                new DeleteCommand("delete", "div"),
                new CaseCommand("tr", jsonUnitCfg),
                new ExpectedStatusCommand("rs-status", "code"),

                new BrowserCommand("div", capabilities),

                new SetVarCommand("span"),

                new EventCheckReplyCommand("event-check", "div", eventProcessor),
                new EventSendCommand("event-send", "div", eventProcessor),

                new KVCheckCommand("db-kv-check", "div", keyValueRepository, jsonUnitCfg)
        );
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