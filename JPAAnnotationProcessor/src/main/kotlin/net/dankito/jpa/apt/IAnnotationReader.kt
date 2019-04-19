package net.dankito.jpa.apt

import net.dankito.jpa.apt.config.*


/**
 * The rationale for this class / interface is to separate the logic for reading annotation values - which could be
 * done at runtime via Reflection or as here at build time via APT - from the logic of processing JPA based
 * annotations, which is implemented in EntityConfigurationReader and ColumnConfigurationReader.
 *
 * So its implementation, AptAnnotationReader, can also be used for other projects. It doesn't have any dependencies
 * on javax.persistence.
 */
interface IAnnotationReader {

    fun getTopLevelEntities(): List<EntityTypeInfo>

    fun registerEntityConfig(entityConfig: EntityConfig)

    fun getEntityConfigForType(type: Type) : EntityConfig?

    fun getEntityConfigsInOrderAdded() : List<EntityConfig>


    fun registerProperty(property: Property)

    fun registerColumn(columnConfig: ColumnConfig)

    fun getColumnConfiguration(property: Property): ColumnConfig?


    @Throws(IllegalArgumentException::class)
    fun checkIfHasNoArgConstructor(entity: EntityTypeInfo): Boolean

    fun <T : Annotation> getAnnotation(entityConfig: EntityConfig, annotationType: Class<T>): T?


    fun getMethods(entityConfig: EntityConfig): List<Method>

    fun <T : Annotation> isAnnotationPresent(entityConfig: EntityConfig, method: Method, annotationType: Class<T>): Boolean {
        return getAnnotation(entityConfig, method, annotationType) != null
    }

    fun <T : Annotation> getAnnotation(entityConfig: EntityConfig, method: Method, annotationType: Class<T>): T?

    fun typeFromQualifiedName(qualifiedName: String): Type


    fun getNonStaticNonTransientFields(entityConfig: EntityConfig): List<Field>

    fun getNonStaticNonAbstractNonTransientMethodsMap(entityConfig: EntityConfig): MutableMap<String, Method>


    fun logInfo(message: String)

    fun logWarn(message: String)
    fun logWarn(message: String, exception: Exception)

    fun logError(message: String)
    fun logError(message: String, exception: Exception)

}