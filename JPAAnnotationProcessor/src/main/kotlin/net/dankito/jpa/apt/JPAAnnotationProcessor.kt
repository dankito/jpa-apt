package net.dankito.jpa.apt

import net.dankito.jpa.apt.config.JpaEntityConfiguration
import net.dankito.jpa.apt.configurationprocessor.json.JsonEntityConfigurationProcessor
import org.slf4j.LoggerFactory
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic


@SupportedAnnotationTypes("javax.persistence.Entity", "javax.persistence.MappedSuperclass", "javax.persistence.Transient")
class JPAAnnotationProcessor : AbstractProcessor() {

    companion object {
        const val ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS: Boolean = true

        private val log = LoggerFactory.getLogger(JPAAnnotationProcessor::class.java)
    }


    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf("javax.persistence.Entity", "javax.persistence.MappedSuperclass", "javax.persistence.Transient")
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Running " + javaClass.simpleName + " for classes ${roundEnv?.rootElements}")

        roundEnv?.let { roundEnv ->
            if (roundEnv.processingOver() || annotations?.isEmpty() ?: true) {
                processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "roundEnv.processingOver() = ${roundEnv.processingOver()}, annotations?.isEmpty() = ${annotations?.isEmpty()}")
                return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS
            }

            if (roundEnv.getRootElements() == null || roundEnv.getRootElements().isEmpty()) {
                processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "No sources to process")
                return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS
            }

            try {
                processAnnotations(roundEnv)
            } catch(e: Exception) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Could not process JPA annotations: $e")
            }
        }

        return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS
    }

    private fun processAnnotations(roundEnv: RoundEnvironment) {
        val context = AnnotationProcessingContext(roundEnv, processingEnv)

        EntityConfigurationReader().readEntityConfigurations(context)
        ColumnConfigurationReader().readEntityColumns(context)

        val entityConfiguration = createResult(context)

        JsonEntityConfigurationProcessor().processConfiguration(entityConfiguration, processingEnv)
    }


    private fun createResult(context: AnnotationProcessingContext): JpaEntityConfiguration {
        return JpaEntityConfiguration(context.getEntityConfigs())
    }

}