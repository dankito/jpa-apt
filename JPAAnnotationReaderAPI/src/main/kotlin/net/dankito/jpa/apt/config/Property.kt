package net.dankito.jpa.apt.config

import java.lang.reflect.ParameterizedType


class Property(val field: Field, val getter: Method?, val setter: Method?) {

    internal constructor() : this(Field(), null, null) // for object deserializers


    private var classType: Class<*>? = null


    fun getType(): Class<*> {
        classType?.let {
            return it
        }

        getPropertyType()?.let { type ->
            try {
                val retrievedType = type.getClassType()
                this.classType = retrievedType

                return retrievedType
            } catch (e: Exception) {
                println("Could not get Class for type ${type.qualifiedName}")
            }
        }

        throw RuntimeException("Could not get type for property $this") // should never come to this
    }

    private fun getPropertyType(): Type? {
        if (field != null) { // TODO: is this allowed that field is null?
            return field.type
        }

        if (getter != null) {
            return getter.returnType
        }

        if (setter != null && setter.hasCountParameters(1)) {
            return setter.parameters[0]
        }

        return null
    }

    fun getGenericType(): Class<*>? {
        val type = getType()

        if (type is ParameterizedType) {
            return getGenericTypeClassFromType(type)
        }

        return null
    }

    private fun getGenericTypeClassFromType(genericType: ParameterizedType): Class<*> {
        return genericType.actualTypeArguments[0] as Class<*>
    }


    override fun toString(): String {
        return field.toString()
    }

}