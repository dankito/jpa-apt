package net.dankito.jpa.apt

import net.dankito.jpa.apt.config.ColumnConfig
import net.dankito.jpa.apt.config.EntityConfig
import net.dankito.jpa.apt.config.EntityTypeInfo
import net.dankito.jpa.apt.config.Property
import java.lang.reflect.Field
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.*
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


    private val entityConfigRegistry = HashMap<Class<*>, EntityConfig>()

    private val propertyRegistry = HashMap<Field, Property>()

    private val columnRegistry = HashMap<Property, ColumnConfig>()

    private val entityTypes = HashMap<Class<*>, EntityTypeInfo>()


    init {
        categorizeElements()
    }

    private fun getElementsFor(annotationClass: Class<out Annotation>) = roundEnv.getElementsAnnotatedWith(annotationClass)

    private fun categorizeElements() {
        roundEnv.rootElements.filter { it is TypeElement && hasEntityOrMappedSuperclassAnnotation(it) }.forEach { element ->
            val typeElement = element as TypeElement

            val entityClass = Class.forName(typeElement.qualifiedName.toString())
            val info = EntityTypeInfo(entityClass, typeElement)

            typeElement.annotationMirrors.forEach { info.classAnnotations.put(it.annotationType, it.elementValues) }
            typeElement.enclosedElements.filter { it.kind == ElementKind.FIELD }.forEach { info.properties.put(it.simpleName.toString(), it as VariableElement) }
            typeElement.enclosedElements.filter { it.kind == ElementKind.METHOD }.forEach { info.methods.put(it.simpleName.toString(), it as ExecutableElement) }

            entityTypes.put(entityClass, info)
        }
    }

    private fun hasEntityOrMappedSuperclassAnnotation(element: TypeElement): Boolean {
        return element.getAnnotation(Entity::class.java) != null || element.getAnnotation(MappedSuperclass::class.java) != null
    }


    fun getAnnotationsForProperty(entityClass: Class<*>, propertyName: String): VariableElement? {
        entityTypes[entityClass]?.let {
            return it.properties[propertyName]
        }

        return null
    }

    fun getAnnotationsForMethod(entityClass: Class<*>, methodName: String): ExecutableElement? {
        entityTypes[entityClass]?.let {
            return it.methods[methodName]
        }

        return null
    }


    fun registerEntityConfig(entityConfig: EntityConfig) {
        entityConfigRegistry.put(entityConfig.entityClass, entityConfig)
    }

    fun getEntityConfigForClass(entityClass: Class<*>) : EntityConfig? {
        return entityConfigRegistry[entityClass]
    }

    fun getEntityConfigs() : List<EntityConfig> {
        return entityConfigRegistry.values.toList()
    }

    fun registerProperty(property: Property) {
        propertyRegistry.put(property.field, property)
    }

    fun registerColumn(columnConfig: ColumnConfig) {
        columnRegistry.put(columnConfig.property, columnConfig)
    }

    fun getColumnConfiguration(property: Property) : ColumnConfig? {
        return columnRegistry[property]
    }

}