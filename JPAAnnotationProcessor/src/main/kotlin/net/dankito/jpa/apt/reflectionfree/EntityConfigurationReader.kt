package net.dankito.jpa.apt.reflectionfree

import java.sql.SQLException
import javax.lang.model.element.Element
import javax.persistence.*


open class EntityConfigurationReader {

    // TODO: extract interface
    open fun readEntityConfigurations(reader: IAnnotationReader) {
        for(topLevelEntityInfo in reader.getTopLevelEntities()) {
            readEntityConfigsDownTheTypeHierarchy(reader, topLevelEntityInfo)
        }
    }

    protected open fun readEntityConfigsDownTheTypeHierarchy(reader: IAnnotationReader, entityTypeInfo: EntityTypeInfo) {
        entityTypeInfo.entityElement?.let { entityElement -> // if entityElement is null then entity has been loaded from a previously built module and its EntityConfig therefore already created
            readEntityConfig(reader, entityTypeInfo, entityElement)
        }

        entityTypeInfo.childClasses.forEach {
            readEntityConfigsDownTheTypeHierarchy(reader, it)
        }
    }


    @Throws(SQLException::class)
    protected open fun readEntityConfig(reader: IAnnotationReader, entityTypeInfo: EntityTypeInfo,
                                        entityElement: Element): EntityConfig {

        reader.logInfo("Reading configuration for " + entityTypeInfo.type.className)

        val entityConfig = createEntityConfig(reader, entityTypeInfo)

        reader.registerEntityConfig(entityConfig)

        entityTypeInfo.superClass?.let { superclass ->
            reader.getEntityConfigForType(superclass.type)?.addChildEntityConfig(entityConfig)
        }

        readEntityAnnotations(reader, entityConfig)
        findLifeCycleEvents(reader, entityConfig)

        return entityConfig
    }

    @Throws(SQLException::class)
    private fun createEntityConfig(reader: IAnnotationReader, entity: EntityTypeInfo): EntityConfig {
        reader.checkIfHasNoArgConstructor(entity)

        val entityConfig = EntityConfig(entity.type)
//        entityConfig.classHierarchy = getClassHierarchy(entity) // TODO

        entityConfig.tableName = entityConfig.type.className // default value, may overwritten by configuration in @Table or @Entity annotation

        return entityConfig
    }


    @Throws(SQLException::class)
    private fun readEntityAnnotations(reader: IAnnotationReader, entityConfig: EntityConfig) {
        readEntityAnnotation(reader, entityConfig)
        readTableAnnotation(reader, entityConfig)
        readAccessAnnotation(reader, entityConfig)
    }

    @Throws(SQLException::class)
    private fun readEntityAnnotation(reader: IAnnotationReader, entityConfig: EntityConfig) {
        reader.getAnnotation(entityConfig, Entity::class.java)?.let { entityAnnotation ->
            val name = entityAnnotation.name
            if(name.isNullOrBlank() == false) {
                entityConfig.tableName = name
            }
        }
    }

    @Throws(SQLException::class)
    private fun readTableAnnotation(reader: IAnnotationReader, entityConfig: EntityConfig) {
        reader.getAnnotation(entityConfig, Table::class.java)?.let { tableAnnotation ->
            entityConfig.tableName = tableAnnotation.name

            entityConfig.catalogName = tableAnnotation.catalog
            entityConfig.schemaName = tableAnnotation.schema

//            entityConfig.uniqueConstraints = tableAnnotation.uniqueConstraints // TODO
//            entityConfig.indexes = tableAnnotation.indexes // TODO: JPA 2.1
        }
    }

    @Throws(SQLException::class)
    private fun readAccessAnnotation(reader: IAnnotationReader, entityConfig: EntityConfig) {
        reader.getAnnotation(entityConfig, Access::class.java)?.let { accessAnnotation ->
            entityConfig.access = accessAnnotation.value
        }
    }


    private fun findLifeCycleEvents(reader: IAnnotationReader, entityConfig: EntityConfig) {
        for(method in reader.getMethods(entityConfig)) {
            checkMethodForLifeCycleEvents(reader, entityConfig, method)
        }
    }

    private fun checkMethodForLifeCycleEvents(reader: IAnnotationReader, entityConfig: EntityConfig, method: Method) {
        if (reader.isAnnotationPresent(entityConfig, method, PrePersist::class.java))
            entityConfig.addPrePersistLifeCycleMethod(method)
        if (reader.isAnnotationPresent(entityConfig, method, PostPersist::class.java))
            entityConfig.addPostPersistLifeCycleMethod(method)
        if (reader.isAnnotationPresent(entityConfig, method, PostLoad::class.java))
            entityConfig.addPostLoadLifeCycleMethod(method)
        if (reader.isAnnotationPresent(entityConfig, method, PreUpdate::class.java))
            entityConfig.addPreUpdateLifeCycleMethod(method)
        if (reader.isAnnotationPresent(entityConfig, method, PostUpdate::class.java))
            entityConfig.addPostUpdateLifeCycleMethod(method)
        if (reader.isAnnotationPresent(entityConfig, method, PreRemove::class.java))
            entityConfig.addPreRemoveLifeCycleMethod(method)
        if (reader.isAnnotationPresent(entityConfig, method, PostRemove::class.java))
            entityConfig.addPostRemoveLifeCycleMethod(method)
    }


//    private fun getClassHierarchy(entityClass: Class<*>) : List<Class<*>> {
//        val classHierarchy = mutableListOf<Class<*>>()
//        var classWalk: Class<*>? = entityClass.superclass
//
//        while (classWalk != null) {
//            if (classIsEntityOrMappedSuperclass(classWalk)) {
//                classHierarchy.add(0, classWalk)
//
//                classWalk = classWalk.superclass
//            }
//            else {
//                break
//            }
//        }
//
//        return classHierarchy
//    }

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