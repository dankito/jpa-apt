package net.dankito.jpa.apt.configurationprocessor.json

import net.dankito.jpa.apt.config.JpaEntityConfiguration
import net.dankito.jpa.apt.configurationprocessor.IEntityConfigurationProcessor
import net.dankito.jpa.apt.serializer.IJPAEntityConfigurationSerializer
import net.dankito.jpa.apt.serializer.json.JsonJPAEntityConfigurationSerializer
import java.io.OutputStreamWriter
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic
import javax.tools.StandardLocation


class JsonEntityConfigurationProcessor(private val entityConfigurationSerializer: IJPAEntityConfigurationSerializer = JsonJPAEntityConfigurationSerializer())
    : IEntityConfigurationProcessor {


    override fun processConfiguration(entityConfiguration: JpaEntityConfiguration, processingEnv: ProcessingEnvironment) {
        try {
            val serializedConfiguration = entityConfigurationSerializer.serializeJPAEntityConfiguration(entityConfiguration)

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