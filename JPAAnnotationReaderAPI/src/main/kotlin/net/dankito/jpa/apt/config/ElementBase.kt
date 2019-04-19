package net.dankito.jpa.apt.config


abstract class ElementBase(val annotations: List<Annotation>, val modifiers: List<Modifier>) {

    open val isPublic = hasModifier(Modifier.Public)

    open val isStatic = hasModifier(Modifier.Static)

    open val isAbstract = hasModifier(Modifier.Abstract)

    open val isTransient = hasModifier(Modifier.Transient)


    open protected fun hasModifier(modifier: Modifier): Boolean {
        return modifiers.contains(modifier)
    }


    open fun <T : Annotation> getAnnotation(annotationType: Class<T>): T? {
        return annotations.firstOrNull { it.annotationClass.java == annotationType } as? T
    }

}