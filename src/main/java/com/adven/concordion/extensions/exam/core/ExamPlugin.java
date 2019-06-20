package com.adven.concordion.extensions.exam.core;

import com.adven.concordion.extensions.exam.core.commands.ExamCommand;

import java.util.List;

public interface ExamPlugin {
    List<ExamCommand> commands();
}
