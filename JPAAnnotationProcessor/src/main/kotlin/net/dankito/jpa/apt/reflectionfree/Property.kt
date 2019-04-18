package net.dankito.jpa.apt.reflectionfree


class Property(val field: Field, val getter: Method?, val setter: Method?) {

    internal constructor() : this(Field(), null, null) // for object deserializers


    override fun toString(): String {
        return field.toString()
    }

}