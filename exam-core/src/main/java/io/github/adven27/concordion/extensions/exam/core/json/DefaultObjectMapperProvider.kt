package io.github.adven27.concordion.extensions.exam.core.json

import com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN
import com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS
import com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES
import com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS
import com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS
import com.fasterxml.jackson.databind.ObjectMapper
import net.javacrumbs.jsonunit.providers.Jackson2ObjectMapperProvider

open class DefaultObjectMapperProvider : Jackson2ObjectMapperProvider {
    private val mapper: ObjectMapper = ObjectMapper().apply {
        configure(USE_BIG_DECIMAL_FOR_FLOATS, true)
        configure(WRITE_BIGDECIMAL_AS_PLAIN, true)
        configure(FAIL_ON_TRAILING_TOKENS, true)
    }
    private val lenientMapper: ObjectMapper = ObjectMapper().apply {
        configure(USE_BIG_DECIMAL_FOR_FLOATS, true)
        configure(WRITE_BIGDECIMAL_AS_PLAIN, true)
        configure(ALLOW_UNQUOTED_FIELD_NAMES, true)
        configure(ALLOW_COMMENTS, true)
        configure(ALLOW_SINGLE_QUOTES, true)
        configure(FAIL_ON_TRAILING_TOKENS, true)
    }

    override fun getObjectMapper(lenient: Boolean) = if (lenient) lenientMapper else mapper
}
