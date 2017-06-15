package net.dankito.jpa.apt.serializer.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import net.dankito.jpa.apt.config.JpaEntityConfiguration
import net.dankito.jpa.apt.serializer.IJPAEntityConfigurationSerializer
import java.io.InputStream
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.net.URL


class JsonJPAEntityConfigurationSerializer : IJPAEntityConfigurationSerializer {

    private val objectMapper = ObjectMapper()

    init {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val module = SimpleModule()

        module.addSerializer(Field::class.java, FieldSerializer())
        module.addDeserializer(Field::class.java, FieldDeserializer())

        module.addSerializer(Method::class.java, MethodSerializer())
        module.addDeserializer(Method::class.java, MethodDeserializer())

        module.addSerializer(Constructor::class.java, ConstructorSerializer())
        module.addDeserializer(Constructor::class.java, ConstructorDeserializer())

        objectMapper.registerModule(module)

        // only serialize fields
//        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
//        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    }


    override fun serializeJPAEntityConfiguration(entityConfiguration: JpaEntityConfiguration): String {
        return objectMapper.writeValueAsString(entityConfiguration)
    }

    override fun deserializeJPAEntityConfiguration(serializedEntityConfiguration: String): JpaEntityConfiguration {
        return objectMapper.readValue(serializedEntityConfiguration, JpaEntityConfiguration::class.java)
    }

    override fun deserializeJPAEntityConfiguration(serializedEntityConfigurationUrl: URL): JpaEntityConfiguration {
        return objectMapper.readValue(serializedEntityConfigurationUrl, JpaEntityConfiguration::class.java)
    }

    override fun deserializeJPAEntityConfiguration(inputStream: InputStream): JpaEntityConfiguration {
        return objectMapper.readValue(inputStream, JpaEntityConfiguration::class.java)
    }

}