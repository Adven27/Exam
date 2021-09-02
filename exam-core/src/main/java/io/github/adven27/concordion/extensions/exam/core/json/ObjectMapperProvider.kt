package io.github.adven27.concordion.extensions.exam.core.json

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.adven27.concordion.extensions.exam.core.ExamExtension
import net.javacrumbs.jsonunit.providers.Jackson2ObjectMapperProvider

class ObjectMapperProvider : Jackson2ObjectMapperProvider {
    override fun getObjectMapper(lenient: Boolean): ObjectMapper =
        ExamExtension.JACKSON_2_OBJECT_MAPPER_PROVIDER.getObjectMapper(lenient)
}
