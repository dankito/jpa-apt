package net.dankito.jpa.apt

import net.dankito.jpa.apt.config.Type
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType


data class EntityTypeInfo(val type: Type,
                          val entityElement: TypeElement?,
                          val classAnnotations: Map<DeclaredType, Map<out ExecutableElement, AnnotationValue>> = HashMap(),
                          val properties: Map<String, VariableElement> = HashMap(),
                          val methods: Map<String, ExecutableElement> = HashMap(),
                          var superClass: EntityTypeInfo? = null,
                          val childClasses: MutableList<EntityTypeInfo> = ArrayList()
) {

    fun addChildClass(entityType: EntityTypeInfo) {
        childClasses.add(entityType)
    }


    override fun toString(): String {
        return type.toString()
    }

}