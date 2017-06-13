package net.dankito.jpa.apt

import org.slf4j.LoggerFactory
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic


@SupportedAnnotationTypes("javax.persistence.*")
class JPAAnnotationProcessor : AbstractProcessor() {

    companion object {
        const val ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS: Boolean = true

        private val log = LoggerFactory.getLogger(JPAAnnotationProcessor::class.java)
    }


    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Running " + javaClass.simpleName)

        roundEnv?.let { roundEnv ->
            if (roundEnv.processingOver() || annotations?.isEmpty() ?: true) {
                return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS
            }

            if (roundEnv.getRootElements() == null || roundEnv.getRootElements().isEmpty()) {
                processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "No sources to process")
                return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS
            }

            val context = AnnotationProcessingContext(roundEnv)

            EntityConfigurationReader().readEntityConfigurations(context)
        }

        return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS
    }

}