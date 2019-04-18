package net.dankito.jpa.apt.reflectionfree

import net.dankito.jpa.apt.reflectionfree.JPAAnnotationProcessor.Companion.KAPT_KOTLIN_GENERATED_OPTION_NAME
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement


@SupportedAnnotationTypes("javax.persistence.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
class JPAAnnotationProcessor : AbstractProcessor() {

    companion object {
        const val ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS: Boolean = true

        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
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

        // TODO: needed?
        val kotlinGeneratedPath = (processingEnv.options["kapt.kotlin.generated"] as? String)?.replace("kaptKotlin", "kapt")
//        processor.logInfo("Writing generated classes to $kotlinGeneratedPath") // ??

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

//        val entityConfiguration = createResult(reader)
//
//        // TODO: remove again
//        reader.logInfo("Found ${entityConfiguration.entities.size} entities:")
//        entityConfiguration.entities.forEach { entityConfig ->
//            reader.logInfo(entityConfig.entityClass.name)
//        }
//
////        EntitiesWithLazyLoadingPropertiesSourceCodeGenerator()
////                .createEntitiesWithLazyLoadingPropertiesSubClasses(entityConfiguration, processingEnv)
//
//        SourceCodeGeneratorEntityConfigurationProcessor().processConfiguration(entityConfiguration, processingEnv)
    }


//    private fun createResult(reader: IAnnotationReader): JPAEntityConfiguration {
//        return JPAEntityConfiguration(reader.getEntityConfigsInOrderAdded())
//    }

}