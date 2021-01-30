package net.dankito.jpa.apt.config

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import java.lang.reflect.Constructor
import java.util.*
import javax.persistence.AccessType
import javax.persistence.CascadeType
import javax.persistence.InheritanceType
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet
import java.lang.Thread.currentThread

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator::class,
        property = "type")
open class EntityConfig(val type: Type) {

    internal constructor() : this(Type()) // for object deserializers


    open lateinit var tableName: String

    // TODO: call that one from parent? (or set value when parent's value is set?)
    open var access: AccessType? = null // TODO: may not be null

    open lateinit var idColumn: ColumnConfig
    open var versionColumn: ColumnConfig? = null

    open var columns = ArrayList<ColumnConfig>()
        protected set

    open var parentEntity: EntityConfig? = null
        protected set

    open val childEntities = ArrayList<EntityConfig>()


    // @Table Annotation settings
    open var catalogName: String? = null
    open var schemaName: String? = null
//    var uniqueConstraints: Array<UniqueConstraint> = arrayOf() // TODO: make serializable
//    var indexes: Array<Index> = arrayOf() // TODO: JPA 2.1

    // inheritance
    open var classHierarchy: List<Class<*>> = mutableListOf()
    protected open var inheritanceTopLevelEntityConfig: EntityConfig? = null
    protected open var topDownInheritanceHierarchy: MutableList<EntityConfig>? = null
    protected open var subClassEntityConfigs: MutableSet<EntityConfig> = HashSet()
    open var inheritance: InheritanceType? = null

    // Life Cycle Events
    open var prePersistLifeCycleMethods = ArrayList<Method>()
        protected set
    open var postPersistLifeCycleMethods = ArrayList<Method>()
        protected set
    open var postLoadLifeCycleMethods = ArrayList<Method>()
        protected set
    open var preUpdateLifeCycleMethods = ArrayList<Method>()
        protected set
    open var postUpdateLifeCycleMethods = ArrayList<Method>()
        protected set
    open var preRemoveLifeCycleMethods = ArrayList<Method>()
        protected set
    open var postRemoveLifeCycleMethods = ArrayList<Method>()
        protected set


    protected open var isIdColumnSet = false

    protected open var areInheritedColumnsLoaded = false
    protected open var columnsIncludingInheritedOnes = LinkedHashSet<ColumnConfig>()

    protected open var areInheritedColumnsWithCascadePersistLoaded = false
    protected open var columnsWithCascadePersistIncludingInheritedOnes = LinkedHashSet<ColumnConfig>()

    protected open var areInheritedColumnsWithCascadeMergeLoaded = false
    protected open var columnsWithCascadeMergeIncludingInheritedOnes = LinkedHashSet<ColumnConfig>()

    private var areInheritedColumnsWithCascadeRemoveLoaded = false
    private var columnsWithCascadeRemoveIncludingInheritedOnes = LinkedHashSet<ColumnConfig>()


    /**
     * Caution: Use these two fields only at runtime, not at build time!
     */
    private var entityClass: Class<*>? = null

    protected open val methods = mutableMapOf<Method, java.lang.reflect.Method>()


    open fun addPrePersistLifeCycleMethod(method: Method) {
        prePersistLifeCycleMethods.add(method)
    }

    open fun addPostPersistLifeCycleMethod(method: Method) {
        postPersistLifeCycleMethods.add(method)
    }

    open fun addPostLoadLifeCycleMethod(method: Method) {
        postLoadLifeCycleMethods.add(method)
    }

    open fun addPreUpdateLifeCycleMethod(method: Method) {
        preUpdateLifeCycleMethods.add(method)
    }

    open fun addPostUpdateLifeCycleMethod(method: Method) {
        postUpdateLifeCycleMethods.add(method)
    }

    open fun addPreRemoveLifeCycleMethod(method: Method) {
        preRemoveLifeCycleMethods.add(method)
    }

    open fun addPostRemoveLifeCycleMethod(method: Method) {
        postRemoveLifeCycleMethods.add(method)
    }

    open fun invokePrePersistLifeCycleMethod(data: Any) {
        invokeMethods(prePersistLifeCycleMethods, data)

        parentEntity?.invokePrePersistLifeCycleMethod(data)
    }

    open fun invokePostPersistLifeCycleMethod(data: Any) {
        invokeMethods(postPersistLifeCycleMethods, data)

        parentEntity?.invokePostPersistLifeCycleMethod(data)
    }

    open fun invokePostLoadLifeCycleMethod(data: Any) {
        invokeMethods(postLoadLifeCycleMethods, data)

        parentEntity?.invokePostLoadLifeCycleMethod(data)
    }

    open fun invokePreUpdateLifeCycleMethod(data: Any) {
        invokeMethods(preUpdateLifeCycleMethods, data)

        parentEntity?.invokePreUpdateLifeCycleMethod(data)
    }

    open fun invokePostUpdateLifeCycleMethod(data: Any) {
        invokeMethods(postUpdateLifeCycleMethods, data)

        parentEntity?.invokePostUpdateLifeCycleMethod(data)
    }

    open fun invokePreRemoveLifeCycleMethod(data: Any) {
        invokeMethods(preRemoveLifeCycleMethods, data)

        parentEntity?.invokePreRemoveLifeCycleMethod(data)
    }

    open fun invokePostRemoveLifeCycleMethod(data: Any) {
        invokeMethods(postRemoveLifeCycleMethods, data)

        parentEntity?.invokePostRemoveLifeCycleMethod(data)
    }

    protected open fun invokeMethods(methods: List<Method>, data: Any) {
        for(method in methods) {
            invokeMethod(method, data)
        }
    }

    protected open fun invokeMethod(method: Method, data: Any) {
        try {
            val javaLangMethod = getJavaLangMethod(method)

            if (javaLangMethod.isAccessible == false) {
                javaLangMethod.isAccessible = true
            }

            javaLangMethod.invoke(data)
        } catch (ex: Exception) {
            println("Could not invoke method ${type.className}.${method.name}: $ex")
        }

    }

    /**
     * Call this method only at runtime, not at build time, as at build time class may is not already compiled!
     */
    open fun getEntityClass(): Class<*> {
        entityClass?.let {
            return it
        }

        val retrievedClass = try {
            Class.forName(type.qualifiedName)
        } catch (e: ClassNotFoundException) {
            currentThread().contextClassLoader.loadClass(type.qualifiedName)
        }

        this.entityClass = retrievedClass

        return retrievedClass
    }

    open fun getNoArgConstructor(): Constructor<*> {
        try {
            val noArgConstructor = getEntityClass().getDeclaredConstructor()

            if (noArgConstructor.isAccessible == false) {
                noArgConstructor.isAccessible = true
            }

            return noArgConstructor
        } catch (e: Exception) {
            println("Could not get no-arg constructor for class ${type.qualifiedName}: " + e)
        }

        // should actually never come to this as we already checked with IAnnotationReader.checkIfHasNoArgConstructor()
        // that a no-arg constructor exists
        throw IllegalStateException("Could not find a no-arg constructor for class ${type.qualifiedName}. Please " +
                "provide it in order to make it instantiatable")
    }

    /**
     * Call this method only at runtime, not at build time, as at build time class and therefore method may is not
     * already compiled!
     */
    protected open fun getJavaLangMethod(method: Method): java.lang.reflect.Method {
        methods[method]?.let {
            return it
        }

        val retrievedMethod = getEntityClass().getDeclaredMethod(method.name,
                *method.parameters.map { Class.forName(it.qualifiedName) }.toTypedArray())

        methods.put(method, retrievedMethod)

        return retrievedMethod
    }


    open fun addColumn(column: ColumnConfig) {
        columns.add(column)
    }

    open fun addChildEntityConfig(entityConfig: EntityConfig) {
        childEntities.add(entityConfig)

        entityConfig.parentEntity = this

        if(isIdColumnSet) {
            setIdColumnOnChildEntitiesRecursively(idColumn)
        }

        versionColumn?.let {
            setVersionColumnOnChildEntitiesRecursively(it)
        }
    }


    open fun setIdColumnAndSetItOnChildEntities(idColumn: ColumnConfig) {
        this.idColumn = idColumn

        this.isIdColumnSet = true

        setIdColumnOnChildEntitiesRecursively(idColumn)
    }

    protected open fun setIdColumnOnChildEntitiesRecursively(idColumn: ColumnConfig) {
        for(childEntity in childEntities) {
            setIdColumnOnChildEntitiesRecursively(childEntity, idColumn)
        }
    }

    protected open fun setIdColumnOnChildEntitiesRecursively(childEntity: EntityConfig, idColumn: ColumnConfig) {
        childEntity.idColumn = idColumn

        for(subChild in childEntity.childEntities) {
            setIdColumnOnChildEntitiesRecursively(subChild, idColumn)
        }
    }

    open fun setVersionColumnAndSetItOnChildEntities(versionColumn: ColumnConfig?) {
        this.versionColumn = versionColumn

        setVersionColumnOnChildEntitiesRecursively(versionColumn)
    }

    protected open fun setVersionColumnOnChildEntitiesRecursively(versionColumn: ColumnConfig?) {
        for(childEntity in childEntities) {
            setVersionColumnOnChildEntitiesRecursively(childEntity, versionColumn)
        }
    }

    protected open fun setVersionColumnOnChildEntitiesRecursively(childEntity: EntityConfig, versionColumn: ColumnConfig?) {
        childEntity.versionColumn = versionColumn

        for(subChild in childEntity.childEntities) {
            setVersionColumnOnChildEntitiesRecursively(subChild, versionColumn)
        }
    }


    open fun isVersionColumnSet(): Boolean {
        return versionColumn != null
    }

    open fun hasParentEntity(): Boolean {
        return parentEntity != null
    }


    open fun getColumnsIncludingInheritedOnes(): Collection<ColumnConfig> {
        if(areInheritedColumnsLoaded == false) {
            loadColumnsIncludingInheritedOnes(this)
        }

        return columnsIncludingInheritedOnes
    }

    protected open fun loadColumnsIncludingInheritedOnes(entityConfig: EntityConfig) {
        entityConfig.parentEntity?.let { parentEntity ->
            loadColumnsIncludingInheritedOnes(parentEntity)
        }

        columnsIncludingInheritedOnes.addAll(entityConfig.columns)
    }


    open fun getColumnsWithCascadePersistIncludingInheritedOnes(): Collection<ColumnConfig> {
        if(areInheritedColumnsWithCascadePersistLoaded == false) {
            loadColumnsWithCascadeIncludingInheritedOnes(this, columnsWithCascadePersistIncludingInheritedOnes, CascadeType.PERSIST)
        }

        return columnsWithCascadePersistIncludingInheritedOnes
    }

    open fun getColumnsWithCascadeMergeIncludingInheritedOnes(): Collection<ColumnConfig> {
        if(areInheritedColumnsWithCascadeMergeLoaded == false) {
            loadColumnsWithCascadeIncludingInheritedOnes(this, columnsWithCascadeMergeIncludingInheritedOnes, CascadeType.MERGE)
        }

        return columnsWithCascadeMergeIncludingInheritedOnes
    }

    open fun getColumnsWithCascadeRemoveIncludingInheritedOnes(): Collection<ColumnConfig> {
        if(areInheritedColumnsWithCascadeRemoveLoaded == false) {
            loadColumnsWithCascadeIncludingInheritedOnes(this, columnsWithCascadeRemoveIncludingInheritedOnes, CascadeType.REMOVE)
        }

        return columnsWithCascadeRemoveIncludingInheritedOnes
    }

    protected open fun loadColumnsWithCascadeIncludingInheritedOnes(entityConfig: EntityConfig, collectionToAddTo: MutableCollection<ColumnConfig>, cascadeType: CascadeType) {
        entityConfig.parentEntity?.let { parentEntity ->
            loadColumnsIncludingInheritedOnes(parentEntity)
        }

        collectionToAddTo.addAll(entityConfig.columns.filter {
            it.cascade.contains(cascadeType) || it.cascade.contains(CascadeType.ALL)
        })
    }


    override fun toString(): String {
        return "$tableName (${type.qualifiedName})"
    }

}