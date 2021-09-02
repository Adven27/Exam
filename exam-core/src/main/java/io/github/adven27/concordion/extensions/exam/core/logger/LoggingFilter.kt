package io.github.adven27.concordion.extensions.exam.core.logger

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Level.toLevel
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.spi.FilterReply
import ch.qos.logback.core.spi.FilterReply.ACCEPT
import ch.qos.logback.core.spi.FilterReply.DENY
import ch.qos.logback.core.spi.FilterReply.NEUTRAL
import io.github.adven27.concordion.extensions.exam.core.ExamExtension
import org.slf4j.Marker

class ExamDelegatingFilter : TurboFilter() {
    override fun decide(
        marker: Marker?,
        logger: Logger?,
        level: Level?,
        format: String?,
        params: Array<out Any>?,
        t: Throwable?
    ): FilterReply = ExamExtension.LOGGING_FILTER.decide(marker, logger, level, format, params, t)
}

open class LoggerLevelFilter(
    protected val loggerLevel: Map<String, String> = mapOf("io.github.adven27" to "info")
) : TurboFilter() {
    override fun decide(
        marker: Marker?,
        logger: Logger,
        level: Level,
        format: String?,
        params: Array<Any?>?,
        t: Throwable?
    ): FilterReply = loggerLevel.entries.firstOrNull { logger.name.startsWith(it.key) }?.let { (_, lvl) ->
        if (level.isGreaterOrEqual(toLevel(lvl))) ACCEPT else DENY
    } ?: NEUTRAL
}
