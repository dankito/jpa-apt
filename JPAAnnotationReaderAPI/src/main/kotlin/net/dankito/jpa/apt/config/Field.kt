package net.dankito.jpa.apt.config

import java.lang.reflect.Field


open class Field @JvmOverloads constructor(val name: String,
                                      val type: Type,
                                      annotations: List<Annotation> = listOf(),
                                      modifiers: List<Modifier> = listOf())
    : ElementBase(annotations, modifiers) {

    companion object {
        private val CouldNotFindJavaField = net.dankito.jpa.apt.config.Field::class.java.getDeclaredField("javaFieldProperty")
    }


    internal constructor() : this("", Type(), listOf(), listOf()) // for object deserializers


    private var javaFieldProperty: Field? = null


    @Throws(IllegalArgumentException::class, IllegalAccessException::class)
    operator fun get(obj: Any?): Any? {
        try {
            obj?.let { containingObject ->
                return getJavaField(containingObject).get(obj)
            }
        } catch (e: Exception) {
            println("Could not get value for field $this of object $obj: " + e)
        }

        return null
    }

    @Throws(IllegalArgumentException::class, IllegalAccessException::class)
    operator fun set(obj: Any?, value: Any?) {
        try {
            obj?.let { containingObject ->
                getJavaField(containingObject).set(obj, value)
            }
        } catch (e: Exception) {
            println("Could not set value for field $this of object $obj: " + e)
        }
    }

    // TODO: this works only for already compiled classes, that is for classes that are not defined in the
    //  same library / project that calls (k)apt!
    protected open fun getJavaField(containingObject: Any): Field {
        javaFieldProperty?.let {
            return it
        }

        findDeclaredField(containingObject)?.let { field ->
            if (field.isAccessible == false) {
                field.isAccessible = true
            }

            this.javaFieldProperty = field
            return field
        }
        ?: run {
            javaFieldProperty = CouldNotFindJavaField
            return CouldNotFindJavaField
        }
    }

    protected open fun findDeclaredField(containingObject: Any): Field? {
        var clazz: Class<*>? = containingObject::class.java

        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name)
            } catch (ignored: Exception) { } // e. g. NoSuchFieldException

            clazz = clazz.superclass
        }

        return null
    }


    override fun toString(): String {
        return "$type $name"
    }

}