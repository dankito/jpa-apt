package net.dankito.jpa.apt.config


open class Type @JvmOverloads constructor(
        val className: String,
        val packageName: String,
        /**
         * The package name and class name combined
         */
        val qualifiedName: String,
        val genericArguments: List<Type> = listOf(),
        val isEnum: Boolean = false
) {

    companion object {
        val CouldNotLoadClass: Class<*> = Void.TYPE
    }


    internal constructor() : this("", "", "") // for object deserializers


    protected var classTypeProperty: Class<*>? = null


    open fun isOfType(typeClass: Class<*>?): Boolean {
        return qualifiedName == typeClass?.name
    }

    open val isBooleanType: Boolean
        get() {

        return when (qualifiedName) {
            "boolean", // primitive boolean
            Boolean::class.java.name,
            java.lang.Boolean::class.java.name-> true
            else -> false
        }
    }

    open val isVoidType: Boolean
        get() {
            return when (qualifiedName) {
                Void.TYPE.name,
                Unit::class.java.name -> true
                else -> false
            }
        }

    /**
     * TODO: this works only for already compiled classes, that is for classes that are not defined in the
     * same library / project that calls (k)apt!
     *
     * We except that canBeAssignedTo() only gets called for Java base types like String, Collection, ... and other
     * known classes.
     */
    open fun canBeAssignedTo(otherType: Class<*>): Boolean {
        return otherType.isAssignableFrom(getClassType())
    }

    // TODO: this works only for already compiled classes, that is for classes that are not defined in the
    //  same library / project that calls (k)apt!
    internal open fun getClassType(): Class<*> {
        classTypeProperty?.let {
            return it
        }

        getPrimitiveTypeClass(qualifiedName)?.let { primitiveTypeClass ->
            this.classTypeProperty = primitiveTypeClass
            return primitiveTypeClass
        }

        try {
            val classType = Class.forName(qualifiedName)
            this.classTypeProperty = classType
            return classType
        } catch (e: Exception) {
            println("Could not load class for type $qualifiedName: " + e)

            classTypeProperty = CouldNotLoadClass
            return CouldNotLoadClass
        }
    }

    protected open fun getPrimitiveTypeClass(qualifiedName: String): Class<*>? {
        return when (qualifiedName) {
            Boolean::class.javaPrimitiveType?.name -> Boolean::class.javaPrimitiveType
            Char::class.javaPrimitiveType?.name -> Char::class.javaPrimitiveType
            Byte::class.javaPrimitiveType?.name -> Byte::class.javaPrimitiveType
            Short::class.javaPrimitiveType?.name -> Short::class.javaPrimitiveType
            Int::class.javaPrimitiveType?.name -> Int::class.javaPrimitiveType
            Long::class.javaPrimitiveType?.name -> Long::class.javaPrimitiveType
            Float::class.javaPrimitiveType?.name -> Float::class.javaPrimitiveType
            Double::class.javaPrimitiveType?.name -> Double::class.javaPrimitiveType
            else -> null
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Type) return false

        if (qualifiedName != other.qualifiedName) return false

        return true
    }

    override fun hashCode(): Int {
        return qualifiedName.hashCode()
    }


    override fun toString(): String {
        return qualifiedName
    }

}