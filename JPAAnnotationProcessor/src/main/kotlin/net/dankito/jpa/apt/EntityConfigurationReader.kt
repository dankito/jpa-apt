package net.dankito.jpa.apt

import net.dankito.jpa.apt.config.EntityConfig
import net.dankito.jpa.apt.config.EntityTypeInfo
import net.dankito.jpa.apt.reflection.ReflectionHelper
import java.lang.reflect.Method
import java.sql.SQLException
import javax.lang.model.element.Element
import javax.persistence.*
import javax.tools.Diagnostic


class EntityConfigurationReader(private val reflectionHelper: ReflectionHelper = ReflectionHelper()) {

    fun readEntityConfigurations(context: AnnotationProcessingContext) {
        for(topLevelEntityInfo in context.getTopLevelEntities()) {
            readEntityConfigsDownTheTypeHierarchy(context, topLevelEntityInfo, listOf())
        }
    }

    private fun readEntityConfigsDownTheTypeHierarchy(context: AnnotationProcessingContext, entityTypeInfo: EntityTypeInfo, currentInheritanceTypeSubEntities: List<EntityConfig>) {
        entityTypeInfo.entityElement?.let { entityElement -> // if entityElement is null then entity has been loaded from a previously built module and its EntityConfig therefore already created
            readEntityConfig(context, entityTypeInfo, entityElement, currentInheritanceTypeSubEntities)
        }

        entityTypeInfo.childClasses.forEach {
            readEntityConfigsDownTheTypeHierarchy(context, it, currentInheritanceTypeSubEntities)
        }
    }


    @Throws(SQLException::class)
    private fun readEntityConfig(context: AnnotationProcessingContext, entityTypeInfo: EntityTypeInfo, entityElement: Element,
                                 currentInheritanceTypeSubEntities: List<EntityConfig>): EntityConfig {
        context.processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Reading configuration for " + entityTypeInfo.entityClass.simpleName)

        val entityConfig = createEntityConfig(entityTypeInfo.entityClass, currentInheritanceTypeSubEntities)

        context.registerEntityConfig(entityConfig)

        entityTypeInfo.superClassInfo?.let { context.getEntityConfigForClass(it.entityClass)?.let { superClassEntityConfig ->
            superClassEntityConfig.addChildEntityConfig(entityConfig)
        } }

        readEntityAnnotations(entityConfig, entityElement)
        findLifeCycleEvents(entityConfig)

        return entityConfig
    }

    @Throws(SQLException::class)
    private fun createEntityConfig(entityClass: Class<*>, currentInheritanceTypeSubEntities: List<EntityConfig>): EntityConfig {
        var entityConfig: EntityConfig? = null
        val inheritanceStrategy = getInheritanceStrategyIfEntityIsInheritanceStartEntity(entityClass)

        if (inheritanceStrategy == null) {
            val constructor = reflectionHelper.findNoArgConstructor(entityClass)
            entityConfig = EntityConfig(entityClass, constructor)
            entityConfig.classHierarchy = getClassHierarchy(entityClass)
        }
        else {
//            entityConfig = createInheritanceEntityConfig(entityClass, inheritanceStrategy, currentInheritanceTypeSubEntities)
            entityConfig = EntityConfig(entityClass, reflectionHelper.findNoArgConstructor(entityClass))
        }

        entityConfig.tableName = entityConfig.entityClass.simpleName // default value, may overwritten by configuration in @Table or @Entity annotation

        return entityConfig
    }


    @Throws(SQLException::class)
    private fun readEntityAnnotations(entityConfig: EntityConfig, entityClassElement: Element) {
        readEntityAnnotation(entityConfig, entityClassElement)
        readTableAnnotation(entityConfig, entityClassElement)
        readAccessAnnotation(entityConfig, entityClassElement)
    }

    @Throws(SQLException::class)
    private fun readEntityAnnotation(entityConfig: EntityConfig, entityClassElement: Element) {
        entityClassElement.getAnnotation(Entity::class.java)?.let { entityAnnotation ->
            val name = entityAnnotation.name
            if(name.isNullOrBlank() == false) {
                entityConfig.tableName = name
            }
        }
    }

    @Throws(SQLException::class)
    private fun readTableAnnotation(entityConfig: EntityConfig, entityClassElement: Element) {
        entityClassElement.getAnnotation(Table::class.java)?.let { tableAnnotation ->
            entityConfig.tableName = tableAnnotation.name

            entityConfig.catalogName = tableAnnotation.catalog
            entityConfig.schemaName = tableAnnotation.schema
            
//            entityConfig.uniqueConstraints = tableAnnotation.uniqueConstraints // TODO
//            entityConfig.indexes = tableAnnotation.indexes // TODO: JPA 2.1
        }
    }

    @Throws(SQLException::class)
    private fun readAccessAnnotation(entityConfig: EntityConfig, entityClassElement: Element) {
        entityClassElement.getAnnotation(Access::class.java)?.let { accessAnnotation ->
            entityConfig.access = accessAnnotation.value
        }
    }


    private fun findLifeCycleEvents(entityConfig: EntityConfig) {
        for(method in entityConfig.entityClass.declaredMethods) {
            checkMethodForLifeCycleEvents(method, entityConfig)
        }
    }

    private fun checkMethodForLifeCycleEvents(method: Method, entityConfig: EntityConfig) {
        if (method.isAnnotationPresent(PrePersist::class.java))
            entityConfig.addPrePersistLifeCycleMethod(method)
        if (method.isAnnotationPresent(PostPersist::class.java))
            entityConfig.addPostPersistLifeCycleMethod(method)
        if (method.isAnnotationPresent(PostLoad::class.java))
            entityConfig.addPostLoadLifeCycleMethod(method)
        if (method.isAnnotationPresent(PreUpdate::class.java))
            entityConfig.addPreUpdateLifeCycleMethod(method)
        if (method.isAnnotationPresent(PostUpdate::class.java))
            entityConfig.addPostUpdateLifeCycleMethod(method)
        if (method.isAnnotationPresent(PreRemove::class.java))
            entityConfig.addPreRemoveLifeCycleMethod(method)
        if (method.isAnnotationPresent(PostRemove::class.java))
            entityConfig.addPostRemoveLifeCycleMethod(method)
    }


    private fun getInheritanceStrategyIfEntityIsInheritanceStartEntity(entityClass: Class<*>): InheritanceType? {

        return null
    }


    private fun getClassHierarchy(entityClass: Class<*>) : List<Class<*>> {
        val classHierarchy = mutableListOf<Class<*>>()
        var classWalk: Class<*>? = entityClass.superclass

        while (classWalk != null) {
            if (classIsEntityOrMappedSuperclass(classWalk)) {
                classHierarchy.add(0, classWalk)

                classWalk = classWalk.superclass
            }
            else {
                break
            }
        }

        return classHierarchy
    }

    private fun classIsEntityOrMappedSuperclass(entityClass: Class<*>): Boolean {
        return classIsEntity(entityClass) || classIsMappedSuperClass(entityClass)
    }

    private fun classIsEntity(entityClass: Class<*>): Boolean {
        return entityClass.isAnnotationPresent(Entity::class.java)
    }

    private fun classIsMappedSuperClass(entityClass: Class<*>): Boolean {
        return entityClass.isAnnotationPresent(MappedSuperclass::class.java)
    }

}