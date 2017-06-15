package net.dankito.jpa.apt.config

import java.lang.reflect.Field
import java.lang.reflect.Method


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

}
