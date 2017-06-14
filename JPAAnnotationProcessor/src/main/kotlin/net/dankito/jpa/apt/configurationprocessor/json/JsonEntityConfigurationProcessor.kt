package net.dankito.jpa.apt.configurationprocessor.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import net.dankito.jpa.apt.config.JpaEntityConfiguration
import net.dankito.jpa.apt.configurationprocessor.IEntityConfigurationProcessor
import net.dankito.jpa.apt.configurationprocessor.json.serializer.*
import org.slf4j.LoggerFactory
import java.io.OutputStreamWriter
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import javax.annotation.processing.ProcessingEnvironment


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

            writeSerializedConfigurationToFile(serializedConfiguration, processingEnv)
        } catch(e: Exception) {
            log.error("Could not serialize EntityConfiguration", e)
        }
    }

    private fun writeSerializedConfigurationToFile(serializedConfiguration: String, processingEnv: ProcessingEnvironment) {
        val packageName = "net.dankito.data_access.database"
        val file = processingEnv.filer.createSourceFile(packageName + ".GeneratedModel")

        val writer = OutputStreamWriter(file.openOutputStream(), "utf-8")

        val classCode = "package " + packageName + ";" + System.lineSeparator() + System.lineSeparator() +
                "public class GeneratedModel {" + System.lineSeparator() + System.lineSeparator() +
                "    public static final String JSON = \"" + serializedConfiguration.replace("\"", "\\\"") + "\";" + System.lineSeparator() + System.lineSeparator() +
                "}"
        writer.write(classCode)

        writer.flush()
        writer.close()
    }

}