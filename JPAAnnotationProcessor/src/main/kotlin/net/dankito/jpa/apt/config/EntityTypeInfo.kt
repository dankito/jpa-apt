package net.dankito.jpa.apt.config

import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType


data class EntityTypeInfo(val entityClass: Class<*>, val entityElement: TypeElement,
                          val classAnnotations: MutableMap<DeclaredType, Map<out ExecutableElement, AnnotationValue>> = HashMap(),
                          val properties: MutableMap<String, VariableElement> = HashMap(),
                          val methods: MutableMap<String, ExecutableElement> = HashMap(),
                          var superClassInfo: EntityTypeInfo? = null, val childClasses: MutableList<EntityTypeInfo> = ArrayList())