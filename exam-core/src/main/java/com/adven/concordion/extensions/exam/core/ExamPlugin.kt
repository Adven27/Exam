package com.adven.concordion.extensions.exam.core

import com.adven.concordion.extensions.exam.core.commands.ExamCommand

interface ExamPlugin {
    fun commands(): List<ExamCommand>
    fun setUp()
    fun tearDown()

    abstract class NoSetUp : ExamPlugin {
        override fun setUp() = Unit
        override fun tearDown() = Unit
    }
}
