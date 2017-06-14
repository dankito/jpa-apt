package net.dankito.jpa.apt.configurationprocessor.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import net.dankito.jpa.apt.config.JpaEntityConfiguration
import net.dankito.jpa.apt.configurationprocessor.IEntityConfigurationProcessor
import org.slf4j.LoggerFactory
import java.lang.reflect.Field
import java.lang.reflect.Method


class JsonEntityConfigurationProcessor : IEntityConfigurationProcessor {

    companion object {
        private val log = LoggerFactory.getLogger(JsonEntityConfigurationProcessor::class.java)
    }


    private val objectMapper = ObjectMapper()

    init {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val module = SimpleModule()

        module.addSerializer(Field::class.java, FieldSerializer())
        module.addDeserializer(Field::class.java, FieldDeserializer())

        module.addSerializer(Method::class.java, MethodSerializer())
        module.addDeserializer(Method::class.java, MethodDeserializer())

        objectMapper.registerModule(module)

        // only serialize fields
//        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
//        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    }


    override fun processConfiguration(entityConfiguration: JpaEntityConfiguration) {
        try {
            val serializedConfiguration = objectMapper.writeValueAsString(entityConfiguration)

            val deserializedConfiguration = objectMapper.readValue(serializedConfiguration, JpaEntityConfiguration::class.java)
            if(deserializedConfiguration != null) {

            }
        } catch(e: Exception) {
            log.error("Could not serialize EntityConfiguration", e)
        }
    }
}