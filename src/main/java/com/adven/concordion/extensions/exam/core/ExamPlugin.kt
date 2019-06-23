package com.adven.concordion.extensions.exam.core

import com.adven.concordion.extensions.exam.core.commands.ExamCommand

interface ExamPlugin {
    fun commands(): List<ExamCommand>
}