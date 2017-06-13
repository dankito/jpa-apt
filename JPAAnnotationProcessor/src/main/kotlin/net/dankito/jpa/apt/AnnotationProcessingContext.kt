package net.dankito.jpa.apt

import net.dankito.jpa.apt.config.EntityConfig
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.persistence.*


class AnnotationProcessingContext(val roundEnv: RoundEnvironment) {

    val entityClasses: Set<out Element> = getElementsFor(Entity::class.java)

    val mappedSuperclasses: Set<out Element> = getElementsFor(MappedSuperclass::class.java)

    
    val columnProperties: Set<out Element> = getElementsFor(Column::class.java)

    val joinColumnProperties: Set<out Element> = getElementsFor(JoinColumn::class.java)


    val oneToOneProperties: Set<out Element> = getElementsFor(OneToOne::class.java)

    val oneToManyProperties: Set<out Element> = getElementsFor(OneToMany::class.java)

    val manyToOneProperties: Set<out Element> = getElementsFor(ManyToOne::class.java)

    val manyToManyProperties: Set<out Element> = getElementsFor(ManyToMany::class.java)


    val transientProperties: Set<out Element> = getElementsFor(Transient::class.java)


    private val entityConfigRegistry = HashMap<Class<*>, EntityConfig<*>>()


    private fun getElementsFor(annotationClass: Class<out Annotation>) = roundEnv.getElementsAnnotatedWith(annotationClass)


    fun registerEntityConfig(entityConfig: EntityConfig<out Any>) {
        entityConfigRegistry.put(entityConfig.entityClass, entityConfig)
    }

}