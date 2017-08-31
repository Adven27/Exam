package com.adven.concordion.extensions.exam.commands;

import com.adven.concordion.extensions.exam.ExamExtension;
import nu.xom.Attribute;
import org.concordion.api.*;
import org.concordion.api.listener.ExecuteEvent;
import org.concordion.api.listener.ExecuteListener;
import org.concordion.internal.util.Announcer;

public class ExamCommand extends AbstractCommand {
    private final String name;
    private final String tag;
    private Announcer<ExecuteListener> listeners = Announcer.to(ExecuteListener.class);

    public ExamCommand(String name, String tag) {
        this.name = name;
        this.tag = tag;
    }

    @Override
    public void execute(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        commandCall.getChildren().processSequentially(evaluator, resultRecorder);
        announceExecuteCompleted(commandCall.getElement());
    }

    private void announceExecuteCompleted(Element element) {
        listeners.announce().executeCompleted(new ExecuteEvent(element));
    }

    public String tag() {
        return tag;
    }

    public String name() {
        return name;
    }

    public void beforeParse(nu.xom.Element elem) {
        Attribute attr = new Attribute(elem.getLocalName(), "");
        attr.setNamespace("e", ExamExtension.NS);
        elem.addAttribute(attr);

        elem.setNamespacePrefix("");
        elem.setNamespaceURI(null);
        elem.setLocalName(tag);
    }
}