package com.adven.concordion.extensions.exam.rest.commands;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

public class CaseCheckCommand extends ExamCommand {

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Element element = commandCall.getElement();
        Element caseTR = element.getParentElement();

        Element cell = new Element("td");
        cell.addAttribute("colspan", "3");
        element.moveChildrenTo(cell);

        Element tr = new Element("tr");
        tr.appendChild(cell);
        caseTR.appendSister(tr);
    }
}