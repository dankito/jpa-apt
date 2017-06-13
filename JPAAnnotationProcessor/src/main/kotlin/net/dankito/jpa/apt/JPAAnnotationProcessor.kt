package net.dankito.jpa.apt

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.element.TypeElement


@SupportedAnnotationTypes("javax.persistence.*")
class JPAAnnotationProcessor : AbstractProcessor() {

    companion object {
        const val ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS: Boolean = true
    }


    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {

        return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS
    }

}