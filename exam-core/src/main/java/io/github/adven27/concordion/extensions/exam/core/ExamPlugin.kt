package io.github.adven27.concordion.extensions.exam.core

import io.github.adven27.concordion.extensions.exam.core.commands.ExamCommand

interface ExamPlugin {
    fun commands(): List<ExamCommand>
    fun setUp()
    fun tearDown()

    abstract class NoSetUp : ExamPlugin {
        override fun setUp() = Unit
        override fun tearDown() = Unit
    }
}
