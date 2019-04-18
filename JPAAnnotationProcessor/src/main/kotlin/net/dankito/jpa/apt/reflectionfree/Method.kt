package net.dankito.jpa.apt.reflectionfree


class Method(val name: String,
             val returnType: Type,
             val parameters: List<Type>,
             annotations: List<Annotation>,
             modifiers: List<Modifier>

) : ElementBase(annotations, modifiers) {


    fun hasNoParameters(): Boolean {
        return hasCountParameters(0)
    }

    fun hasCountParameters(countParameters: Int): Boolean {
        return parameters.size == countParameters
    }


    override fun toString(): String {
        return name
    }

}