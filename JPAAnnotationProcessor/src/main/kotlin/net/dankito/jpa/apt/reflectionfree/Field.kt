package net.dankito.jpa.apt.reflectionfree


class Field(val name: String, val type: Type, annotations: List<Annotation>, modifiers: List<Modifier>)
    : ElementBase(annotations, modifiers) {

    internal constructor() : this("", Type(), listOf(), listOf()) // for object deserializers


    override fun toString(): String {
        return "$type $name"
    }

}