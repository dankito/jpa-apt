package net.dankito.jpa.apt.configurationprocessor.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import net.dankito.jpa.apt.config.JpaEntityConfiguration
import net.dankito.jpa.apt.configurationprocessor.IEntityConfigurationProcessor
import net.dankito.jpa.apt.configurationprocessor.json.serializer.*
import java.io.OutputStreamWriter
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic
import javax.tools.StandardLocation


class JsonEntityConfigurationProcessor : IEntityConfigurationProcessor {

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


    override fun processConfiguration(entityConfiguration: JpaEntityConfiguration, processingEnv: ProcessingEnvironment) {
        try {
            val serializedConfiguration = objectMapper.writeValueAsString(entityConfiguration)

            writeSerializedConfigurationToResourceFile(serializedConfiguration, processingEnv)
        } catch(e: Exception) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Could not serialize EntityConfiguration: $e")
        }
    }

    private fun writeSerializedConfigurationToResourceFile(serializedConfiguration: String, processingEnv: ProcessingEnvironment) {
        val file = processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", "GeneratedModel.json")

        val writer = OutputStreamWriter(file.openOutputStream(), "utf-8")

        writer.write(serializedConfiguration)

        writer.flush()
        writer.close()
    }

}