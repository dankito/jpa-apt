package net.dankito.jpa.apt

import net.dankito.jpa.apt.config.JPAEntityConfiguration
import net.dankito.jpa.apt.configurationprocessor.source.SourceCodeGeneratorEntityConfigurationProcessor
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement


@SupportedAnnotationTypes("javax.persistence.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class JPAAnnotationProcessor : AbstractProcessor() {

    companion object {
        const val ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS: Boolean = true
    }


    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val reader = AptAnnotationReader(processingEnv, roundEnv)

        reader.logInfo("Running " + javaClass.simpleName)

        if (roundEnv.processingOver() || annotations.isEmpty()) {
            reader.logInfo("Nothing to do: roundEnv.processingOver() = ${roundEnv.processingOver()}, " +
                    "annotations.isEmpty() = ${annotations.isEmpty()}")
            return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS
        }

        if (roundEnv.rootElements.isNullOrEmpty()) {
            reader.logInfo("Nothing to do: No sources to process")
            return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS
        }


        try {
            processAnnotations(reader) // TODO: what to return here?
        } catch(e: Exception) {
            reader.logError("Could not process JPA annotations", e)
        }

        return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS
    }

    private fun processAnnotations(reader: IAnnotationReader) {
        EntityConfigurationReader().readEntityConfigurations(reader)
        ColumnConfigurationReader().readEntityColumns(reader)

        val entityConfiguration = createResult(reader)

        // TODO: remove again
        reader.logInfo("Found ${entityConfiguration.entities.size} entities:")
        entityConfiguration.entities.forEach { entityConfig ->
            reader.logInfo(entityConfig.type.qualifiedName)
        }

//        EntitiesWithLazyLoadingPropertiesSourceCodeGenerator()
//                .createEntitiesWithLazyLoadingPropertiesSubClasses(entityConfiguration, processingEnv)

        SourceCodeGeneratorEntityConfigurationProcessor().processConfiguration(entityConfiguration, processingEnv)
    }


    private fun createResult(reader: IAnnotationReader): JPAEntityConfiguration {
        return JPAEntityConfiguration(reader.getEntityConfigsInOrderAdded())
    }

}