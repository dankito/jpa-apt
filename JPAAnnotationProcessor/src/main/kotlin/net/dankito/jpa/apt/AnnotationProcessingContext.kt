package net.dankito.jpa.apt

import net.dankito.jpa.apt.config.ColumnConfig
import net.dankito.jpa.apt.config.EntityConfig
import net.dankito.jpa.apt.config.EntityTypeInfo
import net.dankito.jpa.apt.config.Property
import net.dankito.jpa.apt.configurationprocessor.source.SourceCodeGeneratorEntityConfigurationProcessor
import java.lang.reflect.Field
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.*
import javax.persistence.Entity
import javax.persistence.MappedSuperclass
import javax.tools.Diagnostic


class AnnotationProcessingContext(val roundEnv: RoundEnvironment, val processingEnv: ProcessingEnvironment) {

    private val entityConfigRegistry = LinkedHashMap<Class<*>, EntityConfig>()

    private val entityConfigsInOrderAdded = ArrayList<EntityConfig>()

    private val entityConfigsFromPreviousBuiltProjects = LinkedHashMap<Class<*>, EntityConfig>() // it's very important to keep order so that later on EntityConfigs get created
    // according to their hierarchy (EntityConfigs of base classes have to be created first)

    private val propertyRegistry = HashMap<Field, Property>()

    private val columnRegistry = HashMap<Property, ColumnConfig>()

    private val entityTypes = HashMap<Class<*>, EntityTypeInfo>()


    init {
        readEntityConfigsFromPreviousBuildProjects()

        categorizeElements()
        orderEntitiesByClassHierarchy()
    }

    private fun readEntityConfigsFromPreviousBuildProjects() {
        try {
            val generatedEntityConfigsClass = Class.forName(SourceCodeGeneratorEntityConfigurationProcessor.GeneratedEntityConfigsPackageName + "." +
                    SourceCodeGeneratorEntityConfigurationProcessor.GeneratedEntityConfigsClassName)
            val generatedEntityConfigsInstance = generatedEntityConfigsClass.newInstance()

            val getGeneratedEntityConfigsMethod = generatedEntityConfigsClass.getDeclaredMethod(SourceCodeGeneratorEntityConfigurationProcessor.GetGeneratedEntityConfigsMethodName)
            val generatedEntityConfigs = getGeneratedEntityConfigsMethod.invoke(generatedEntityConfigsInstance) as List<EntityConfig>

            generatedEntityConfigs.forEach { entityConfig ->
                entityConfigsFromPreviousBuiltProjects.put(entityConfig.entityClass, entityConfig)
            }
        } catch(e: Exception) { } // most often the case that there aren't any other entities from other modules / projects
    }

    private fun categorizeElements() {
        val entitiesAndMappedSuperclasses = LinkedHashSet<Element>(roundEnv.getElementsAnnotatedWith(MappedSuperclass::class.java))
        entitiesAndMappedSuperclasses.addAll(roundEnv.getElementsAnnotatedWith(Entity::class.java))

        entityConfigsFromPreviousBuiltProjects.forEach { // also add previously created EntityConfigs so we know them if they are parent classes of this module's entities.
            entityTypes.put(it.key, EntityTypeInfo(it.key, null)) // entityElement can be set to null here as EntityConfig is already known and we don't need to re-read it
        }

        entitiesAndMappedSuperclasses.forEach { element ->
            try {
                createEntityTypeInfoForElement(element)
            } catch(e: Exception) { processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Could not create EntityTypeInfo for element $element: $e", element) }
        }
    }

    private fun createEntityTypeInfoForElement(element: Element) {
        val typeElement = element as TypeElement

        val entityClass = Class.forName(typeElement.qualifiedName.toString())
        val info = EntityTypeInfo(entityClass, typeElement)

        typeElement.annotationMirrors.forEach { info.classAnnotations.put(it.annotationType, it.elementValues) }
        typeElement.enclosedElements.filter { it.kind == ElementKind.FIELD }.forEach { info.properties.put(it.simpleName.toString(), it as VariableElement) }
        typeElement.enclosedElements.filter { it.kind == ElementKind.METHOD }.forEach { info.methods.put(it.simpleName.toString(), it as ExecutableElement) }

        entityTypes.put(entityClass, info)
    }


    private fun orderEntitiesByClassHierarchy() {
        entityTypes.values.forEach { info ->
            val superClass = info.entityClass.superclass
            entityTypes[superClass]?.let { superClassEntityTypeInfo ->
                info.superClassInfo = superClassEntityTypeInfo
                superClassEntityTypeInfo.childClasses.add(info)
            }
        }
    }


    fun getTopLevelEntities(): List<EntityTypeInfo> {
        return entityTypes.values.filter { it.superClassInfo == null }
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
        entityConfigsInOrderAdded.add(entityConfig)
        entityConfigRegistry.put(entityConfig.entityClass, entityConfig)
    }

    fun getEntityConfigForClass(entityClass: Class<*>) : EntityConfig? {
        entityConfigRegistry[entityClass]?.let {
            return it
        }

        return entityConfigsFromPreviousBuiltProjects[entityClass]
    }

    fun getEntityConfigsInOrderAdded() : List<EntityConfig> {
        return entityConfigsInOrderAdded
    }

    fun getAllReadEntityConfigsInOrderAdded() : List<EntityConfig> {
        val allEntityConfigs = LinkedHashMap<Class<*>, EntityConfig>()

        allEntityConfigs.putAll(entityConfigsFromPreviousBuiltProjects)
        allEntityConfigs.putAll(entityConfigRegistry) // EntityConfigs already added by entityConfigsFromPreviousBuiltProjects get overwritten here

        return allEntityConfigs.values.toList()
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