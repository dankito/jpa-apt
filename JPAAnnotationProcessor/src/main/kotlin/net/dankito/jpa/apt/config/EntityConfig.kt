package net.dankito.jpa.apt.config

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.*
import javax.persistence.AccessType
import javax.persistence.InheritanceType
import javax.persistence.UniqueConstraint
import kotlin.collections.ArrayList

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator::class,
        property = "entityClass")
class EntityConfig(val entityClass: Class<*>, val constructor: Constructor<*>) {

    internal constructor() : this(Any::class.java, Any::class.java.declaredConstructors[0]) { // for Jackson

    }

    lateinit var tableName: String

    var access: AccessType? = null

    lateinit var idColumn: ColumnConfig
    var versionColumn: ColumnConfig? = null
    
    var columns = ArrayList<ColumnConfig>()
        private set
    

    // @Table Annotation settings
    var catalogName: String? = null
    var schemaName: String? = null
    @Transient // TODO: make serializable
    var uniqueConstraints: Array<UniqueConstraint> = arrayOf()
//    var indexes: Array<Index> = arrayOf() // TODO: JPA 2.1

    // inheritance
    var classHierarchy: List<Class<*>> = mutableListOf()
    protected var inheritanceTopLevelEntityConfig: EntityConfig? = null
    protected var parentEntityConfig: EntityConfig? = null
    protected var topDownInheritanceHierarchy: MutableList<EntityConfig>? = null
    protected var subClassEntityConfigs: MutableSet<EntityConfig> = HashSet()
    var inheritance: InheritanceType? = null

    // Life Cycle Events
    var prePersistLifeCycleMethods = ArrayList<Method>()
    var postPersistLifeCycleMethods = ArrayList<Method>()
    var postLoadLifeCycleMethods = ArrayList<Method>()
    var preUpdateLifeCycleMethods = ArrayList<Method>()
    var postUpdateLifeCycleMethods = ArrayList<Method>()
    var preRemoveLifeCycleMethods = ArrayList<Method>()
    var postRemoveLifeCycleMethods = ArrayList<Method>()


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
    }

    fun invokePostPersistLifeCycleMethod(data: Any) {
        invokeMethods(postPersistLifeCycleMethods, data)
    }

    fun invokePostLoadLifeCycleMethod(data: Any) {
        invokeMethods(postLoadLifeCycleMethods, data)
    }

    fun invokePreUpdateLifeCycleMethod(data: Any) {
        invokeMethods(preUpdateLifeCycleMethods, data)
    }

    fun invokePostUpdateLifeCycleMethod(data: Any) {
        invokeMethods(postUpdateLifeCycleMethods, data)
    }

    fun invokePreRemoveLifeCycleMethod(data: Any) {
        invokeMethods(preRemoveLifeCycleMethods, data)
    }

    fun invokePostRemoveLifeCycleMethod(data: Any) {
        invokeMethods(postRemoveLifeCycleMethods, data)
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


    override fun toString(): String {
        if(tableName != null) {
            return tableName
        }
        else {
            return entityClass.simpleName
        }
    }

}