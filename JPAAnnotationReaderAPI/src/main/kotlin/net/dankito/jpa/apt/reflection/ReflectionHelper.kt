package net.dankito.jpa.apt.reflection

import net.dankito.jpa.apt.config.Property
import java.lang.reflect.*
import java.util.*
import javax.persistence.Transient


class ReflectionHelper {

    /**
     * Locate the no arg constructor for the class.
     */
    @Throws(IllegalArgumentException::class)
    fun findNoArgConstructor(entityClass: Class<*>): Constructor<*> {
        val constructors: Array<Constructor<*>>
        try {
            constructors = entityClass.declaredConstructors
            // i do this [grossness] to be able to move the Suppress inside the method
        } catch (e: Exception) {
            throw IllegalArgumentException("Can't lookup declared constructors for " + entityClass, e)
        }

        for(constructor in constructors) {
            if (constructor.parameterTypes.isEmpty()) {
                return constructor
            }
        }

        if (entityClass.enclosingClass == null) {
            throw IllegalArgumentException("Can't find a no-arg constructor for " + entityClass)
        }
        else {
            throw IllegalArgumentException("Can't find a no-arg constructor for " + entityClass
                    + ".  Missing static on inner class?")
        }
    }


    fun makeAccessible(property: Property) {
        makeAccessible(property.field)

        property.getter?.let { getter -> makeAccessible(getter) }

        property.setter?.let { setter -> makeAccessible(setter) }
    }

    fun makeAccessible(accessibleObject: AccessibleObject) {
        if (!accessibleObject.isAccessible) {
            try {
                val test = accessibleObject.toString()
                accessibleObject.isAccessible = true
            } catch (e: SecurityException) {
                throw IllegalArgumentException("Could not make " + accessibleObject + " accessible", e)
            }
        }
    }


    fun findProperties(fields: List<Field>, methodsMap: MutableMap<String, Method>) : List<Property> {
        val properties = ArrayList<Property>()

        for(field in fields) {
            val getter = findGetMethod(field, methodsMap)
            val setter = findSetMethod(field, methodsMap)

            val property = Property(field, getter, setter)
            properties.add(property)
        }

        return properties
    }

    private fun findGetMethod(field: Field, methodsMap: MutableMap<String, Method>): Method? {
        val fieldName = getFieldNameWithFirstLetterUpperCase(field)

        var getMethod = methodsMap["get" + fieldName]
        if(isGetMethodForField(getMethod, field.type)) {
            methodsMap.remove(getMethod!!.name)
            return getMethod
        }

        if(field.type == Boolean::class.java) {
            getMethod = methodsMap["is" + fieldName]
            if (isGetMethodForField(getMethod, field.type)) {
                methodsMap.remove(getMethod!!.name)
                return getMethod
            }

            getMethod = methodsMap["has" + fieldName]
            if (isGetMethodForField(getMethod, field.type)) {
                methodsMap.remove(getMethod!!.name)
                return getMethod
            }
        }

        return null
    }

    private fun findSetMethod(field: Field, methodsMap: Map<String, Method>): Method? {
        val fieldName = getFieldNameWithFirstLetterUpperCase(field)

        methodsMap["set" + fieldName]?.let { setMethod ->
            if(setMethod.returnType == Void.TYPE && setMethod.parameterCount == 1 && setMethod.parameterTypes[0] == field.type) {
                return setMethod
            }
        }

        return null
    }

    private fun getFieldNameWithFirstLetterUpperCase(field: Field) = field.name.substring(0, 1).toUpperCase(Locale.ENGLISH) + field.name.substring(1)

    fun getNonStaticNonTransientFields(entityClass: Class<out Any?>): List<Field> {
        val fields = mutableListOf<Field>()

        for(field in entityClass.declaredFields) {
            if(isStaticOrTransientField(field) == false) {
                fields.add(field)
            }
        }

        return fields
    }

    fun getNonStaticNonAbstractNonTransientMethodsMap(entityClass: Class<out Any?>): MutableMap<String, Method> {
        val methods = HashMap<String, Method>()

        entityClass.declaredMethods
                .filter { isNonStaticNonAbstractNonTransientMethod(it) }
                .forEach { methods.put(it.name, it) }

        return methods
    }

    private fun isStaticOrTransientField(field: Field): Boolean {
        return Modifier.isStatic(field.modifiers) || Modifier.isTransient(field.modifiers)
    }

    private fun isNonStaticNonAbstractNonTransientMethod(method: Method): Boolean {
        return isStaticMethod(method) == false && isAbstractMethod(method) == false && method.isAnnotationPresent(Transient::class.java) == false
    }

    private fun isFinalMethod(method: Method): Boolean {
        return Modifier.isFinal(method.modifiers)
    }

    private fun isStaticMethod(method: Method): Boolean {
        return Modifier.isStatic(method.modifiers)
    }

    private fun isAbstractMethod(method: Method): Boolean {
        return Modifier.isAbstract(method.modifiers)
    }


    private fun isGetMethodForField(getMethod: Method?, fieldType: Class<*>): Boolean {
        return getMethod != null && isGetMethod(getMethod) && getMethod.returnType == fieldType
    }

    private fun isGetMethod(method: Method): Boolean {
        val methodName = method.name
        return methodHasNoParameters(method) &&
                ( methodName.startsWith("get") || isBooleanGetMethod(method, methodName) )
    }

    private fun isBooleanGetMethod(method: Method, methodName: String) = (method.returnType == Boolean::class.java && (methodName.startsWith("is") || methodName.startsWith("has")))

    private fun methodHasNoParameters(method: Method): Boolean {
        return method.parameterTypes.size == 0
    }

}