package net.dankito.jpa.apt.serializer.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import net.dankito.jpa.apt.config.JPAEntityConfiguration
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


    override fun serializeJPAEntityConfiguration(entityConfiguration: JPAEntityConfiguration): String {
        return objectMapper.writeValueAsString(entityConfiguration)
    }

    override fun deserializeJPAEntityConfiguration(serializedEntityConfiguration: String): JPAEntityConfiguration {
        return objectMapper.readValue(serializedEntityConfiguration, JPAEntityConfiguration::class.java)
    }

    override fun deserializeJPAEntityConfiguration(serializedEntityConfigurationUrl: URL): JPAEntityConfiguration {
        return objectMapper.readValue(serializedEntityConfigurationUrl, JPAEntityConfiguration::class.java)
    }

    override fun deserializeJPAEntityConfiguration(inputStream: InputStream): JPAEntityConfiguration {
        return objectMapper.readValue(inputStream, JPAEntityConfiguration::class.java)
    }

}