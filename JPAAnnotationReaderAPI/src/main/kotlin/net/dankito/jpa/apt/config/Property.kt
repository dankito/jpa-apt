package net.dankito.jpa.apt.config

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


data class Property(val field: Field, val getter: Method?, val setter: Method?) {

    internal constructor() : this(Property::class.java.getDeclaredField("field"), null, null) // for Jackson


    fun getType(): Class<*> {
        if (field != null) {
            return field.type
        }

        if (getter != null) {
            return getter.getReturnType()
        }

        if (setter != null && setter.getParameterTypes().size == 1) {
            return setter.getParameterTypes()[0]
        }

        throw RuntimeException("Could not get type for property $this") // should never come to this
    }

    fun getGenericType(): Class<*>? {
        if (field.genericType is ParameterizedType) {
            return getGenericTypeClassFromType(field.genericType)
        }

        if (getter != null && getter.genericReturnType is ParameterizedType) {
            return getGenericTypeClassFromType(getter.genericReturnType)
        }

        if (setter != null && setter.parameterTypes.size == 1 && setter.getGenericParameterTypes()[0] is ParameterizedType) {
            return getGenericTypeClassFromType(setter.genericParameterTypes[0])
        }

        return null
    }

    private fun getGenericTypeClassFromType(genericType: Type): Class<*> {
        return (genericType as ParameterizedType).actualTypeArguments[0] as Class<*>
    }

}
