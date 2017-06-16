package net.dankito.jpa.apt.config

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.*
import javax.persistence.AccessType
import javax.persistence.CascadeType
import javax.persistence.InheritanceType
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator::class,
        property = "entityClass")
open class EntityConfig(val entityClass: Class<*>, val constructor: Constructor<*>) {

    /**
     * As we currently only support no-arg constructors, this convenience constructor automatically gets no-arg constructor from passed class
     */
    constructor(entityClass: Class<*>) : this(entityClass, entityClass.getDeclaredConstructor())

    internal constructor() : this(Any::class.java) { // for Jackson

    }

    lateinit var tableName: String

    // TODO: call that one from parent? (or set value when parent's value is set?)
    var access: AccessType? = null // TODO: may not be null

    lateinit var idColumn: ColumnConfig
    var versionColumn: ColumnConfig? = null
    
    var columns = ArrayList<ColumnConfig>()
        private set

    var parentEntity: EntityConfig? = null
        private set

    val childEntities = ArrayList<EntityConfig>()
    

    // @Table Annotation settings
    var catalogName: String? = null
    var schemaName: String? = null
//    var uniqueConstraints: Array<UniqueConstraint> = arrayOf() // TODO: make serializable
//    var indexes: Array<Index> = arrayOf() // TODO: JPA 2.1

    // inheritance
    var classHierarchy: List<Class<*>> = mutableListOf()
    protected var inheritanceTopLevelEntityConfig: EntityConfig? = null
    protected var topDownInheritanceHierarchy: MutableList<EntityConfig>? = null
    protected var subClassEntityConfigs: MutableSet<EntityConfig> = HashSet()
    var inheritance: InheritanceType? = null

    // Life Cycle Events
    var prePersistLifeCycleMethods = ArrayList<Method>()
        private set
    var postPersistLifeCycleMethods = ArrayList<Method>()
        private set
    var postLoadLifeCycleMethods = ArrayList<Method>()
        private set
    var preUpdateLifeCycleMethods = ArrayList<Method>()
        private set
    var postUpdateLifeCycleMethods = ArrayList<Method>()
        private set
    var preRemoveLifeCycleMethods = ArrayList<Method>()
        private set
    var postRemoveLifeCycleMethods = ArrayList<Method>()
        private set


    private var areInheritedColumnsLoaded = false
    private var columnsIncludingInheritedOnes = LinkedHashSet<ColumnConfig>()

    private var areInheritedColumnsWithCascadePersistLoaded = false
    private var columnsWithCascadePersistIncludingInheritedOnes = LinkedHashSet<ColumnConfig>()

    private var areInheritedColumnsWithCascadeRemoveLoaded = false
    private var columnsWithCascadeRemoveIncludingInheritedOnes = LinkedHashSet<ColumnConfig>()


    fun addPrePersistLifeCycleMethod(method: Method) {
        prePersistLifeCycleMethods.add(method)
    }

    fun addPostPersistLifeCycleMethod(method: Method) {
        postPersistLifeCycleMethods.add(method)
    }

    fun addPostLoadLifeCycleMethod(method: Method) {
        postLoadLifeCycleMethods.add(method)
    }

    fun addPreUpdateLifeCycleMethod(method: Method) {
        preUpdateLifeCycleMethods.add(method)
    }

    fun addPostUpdateLifeCycleMethod(method: Method) {
        postUpdateLifeCycleMethods.add(method)
    }

    fun addPreRemoveLifeCycleMethod(method: Method) {
        preRemoveLifeCycleMethods.add(method)
    }

    fun addPostRemoveLifeCycleMethod(method: Method) {
        postRemoveLifeCycleMethods.add(method)
    }

    fun invokePrePersistLifeCycleMethod(data: Any) {
        invokeMethods(prePersistLifeCycleMethods, data)

        parentEntity?.invokePrePersistLifeCycleMethod(data)
    }

    fun invokePostPersistLifeCycleMethod(data: Any) {
        invokeMethods(postPersistLifeCycleMethods, data)

        parentEntity?.invokePostPersistLifeCycleMethod(data)
    }

    fun invokePostLoadLifeCycleMethod(data: Any) {
        invokeMethods(postLoadLifeCycleMethods, data)

        parentEntity?.invokePostLoadLifeCycleMethod(data)
    }

    fun invokePreUpdateLifeCycleMethod(data: Any) {
        invokeMethods(preUpdateLifeCycleMethods, data)

        parentEntity?.invokePreUpdateLifeCycleMethod(data)
    }

    fun invokePostUpdateLifeCycleMethod(data: Any) {
        invokeMethods(postUpdateLifeCycleMethods, data)

        parentEntity?.invokePostUpdateLifeCycleMethod(data)
    }

    fun invokePreRemoveLifeCycleMethod(data: Any) {
        invokeMethods(preRemoveLifeCycleMethods, data)

        parentEntity?.invokePreRemoveLifeCycleMethod(data)
    }

    fun invokePostRemoveLifeCycleMethod(data: Any) {
        invokeMethods(postRemoveLifeCycleMethods, data)

        parentEntity?.invokePostRemoveLifeCycleMethod(data)
    }

    private fun invokeMethods(methods: List<Method>, data: Any) {
        for(method in methods) {
            invokeMethod(method, data)
        }
    }

    private fun invokeMethod(method: Method, data: Any) {
        try {
            val isAccessible = method.isAccessible
            if (isAccessible == false) {
                method.isAccessible = true
            }

            method.invoke(data)

            if (isAccessible == false) {
                method.isAccessible = false
            }
        } catch (ex: Exception) {
//            log.error("Could not invoke method " + method.name, ex)
        }

    }


    fun addColumn(column: ColumnConfig) {
        columns.add(column)
    }

    fun addChildEntityConfig(entityConfig: EntityConfig) {
        childEntities.add(entityConfig)

        entityConfig.parentEntity = this
    }


    fun setIdColumnAndSetItOnChildEntities(idColumn: ColumnConfig) {
        this.idColumn = idColumn

        setIdColumnOnChildEntitiesRecursively(idColumn)
    }

    private fun setIdColumnOnChildEntitiesRecursively(idColumn: ColumnConfig) {
        for(childEntity in childEntities) {
            setIdColumnOnChildEntitiesRecursively(childEntity, idColumn)
        }
    }

    private fun setIdColumnOnChildEntitiesRecursively(childEntity: EntityConfig, idColumn: ColumnConfig) {
        childEntity.idColumn = idColumn

        for(subChild in childEntity.childEntities) {
            setIdColumnOnChildEntitiesRecursively(subChild, idColumn)
        }
    }

    fun setVersionColumnAndSetItOnChildEntities(versionColumn: ColumnConfig?) {
        this.versionColumn = versionColumn

        setVersionColumnOnChildEntitiesRecursively(versionColumn)
    }

    private fun setVersionColumnOnChildEntitiesRecursively(versionColumn: ColumnConfig?) {
        for(childEntity in childEntities) {
            setVersionColumnOnChildEntitiesRecursively(childEntity, versionColumn)
        }
    }

    private fun setVersionColumnOnChildEntitiesRecursively(childEntity: EntityConfig, versionColumn: ColumnConfig?) {
        childEntity.versionColumn = versionColumn

        for(subChild in childEntity.childEntities) {
            setVersionColumnOnChildEntitiesRecursively(subChild, versionColumn)
        }
    }



    fun getColumnsIncludingInheritedOnes(): Collection<ColumnConfig> {
        if(areInheritedColumnsLoaded == false) {
            loadColumnsIncludingInheritedOnes(this)
        }

        return columnsIncludingInheritedOnes
    }

    private fun loadColumnsIncludingInheritedOnes(entityConfig: EntityConfig) {
        entityConfig.parentEntity?.let { parentEntity ->
            loadColumnsIncludingInheritedOnes(parentEntity)
        }

        columnsIncludingInheritedOnes.addAll(entityConfig.columns)
    }


    fun getColumnsWithCascadePersistIncludingInheritedOnes(): Collection<ColumnConfig> {
        if(areInheritedColumnsWithCascadePersistLoaded == false) {
            loadColumnsWithCascadeIncludingInheritedOnes(this, columnsWithCascadePersistIncludingInheritedOnes, CascadeType.PERSIST)
        }

        return columnsWithCascadePersistIncludingInheritedOnes
    }

    fun getColumnsWithCascadeRemoveIncludingInheritedOnes(): Collection<ColumnConfig> {
        if(areInheritedColumnsWithCascadeRemoveLoaded == false) {
            loadColumnsWithCascadeIncludingInheritedOnes(this, columnsWithCascadeRemoveIncludingInheritedOnes, CascadeType.REMOVE)
        }

        return columnsWithCascadeRemoveIncludingInheritedOnes
    }

    private fun loadColumnsWithCascadeIncludingInheritedOnes(entityConfig: EntityConfig, collectionToAddTo: MutableCollection<ColumnConfig>, cascadeType: CascadeType) {
        entityConfig.parentEntity?.let { parentEntity ->
            loadColumnsIncludingInheritedOnes(parentEntity)
        }

        collectionToAddTo.addAll(entityConfig.columns.filter { it.cascade.contains(cascadeType) })
    }


    override fun toString(): String {
        if(tableName != null) {
            return tableName
        }
        else {
            return entityClass.simpleName
        }
    }

}