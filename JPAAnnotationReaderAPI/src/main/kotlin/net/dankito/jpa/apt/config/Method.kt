package net.dankito.jpa.apt.config

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


open class Method @JvmOverloads constructor(val name: String,
                                       val returnType: Type,
                                       val parameters: List<Type>,
                                       annotations: List<Annotation> = listOf(),
                                       modifiers: List<Modifier> = listOf()

) : ElementBase(annotations, modifiers) {

    companion object {
        private val CouldNotFindJavaMethod = Any::class.java.getDeclaredMethod("hashCode")
    }


    protected var javaMethodProperty: Method? = null


    fun hasNoParameters(): Boolean {
        return hasCountParameters(0)
    }

    fun hasCountParameters(countParameters: Int): Boolean {
        return parameters.size == countParameters
    }


    @Throws(IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class)
    operator fun invoke(obj: Any?, vararg args: Any): Any? {
        obj?.let { containingObject ->
            try {
                return getJavaMethod(containingObject).invoke(obj, args)
            } catch (e: Exception) {
                println("Could not invoke method $this for object $containingObject: " + e)
            }
        }

        return null
    }


    // TODO: this works only for already compiled classes, that is for classes that are not defined in the
    //  same library / project that calls (k)apt!
    protected open fun getJavaMethod(containingObject: Any): Method {
        javaMethodProperty?.let {
            return it
        }

        findDeclaredMethod(containingObject)?.let { method ->
            if (method.isAccessible == false) {
                method.isAccessible = true
            }

            this.javaMethodProperty = method
            return method
        }
        ?: run {
            javaMethodProperty = CouldNotFindJavaMethod
            return CouldNotFindJavaMethod
        }
    }

    protected open fun findDeclaredMethod(containingObject: Any): Method? {
        var clazz: Class<*>? = containingObject::class.java

        while (clazz != null) {
            try {
                return containingObject.javaClass.getDeclaredMethod(name,
                        *parameters.map { Class.forName(it.qualifiedName) }.toTypedArray())
            } catch (ignored: Exception) { } // e. g. NoSuchFieldException

            clazz = clazz.superclass
        }

        return null
    }


    override fun toString(): String {
        return name
    }

}