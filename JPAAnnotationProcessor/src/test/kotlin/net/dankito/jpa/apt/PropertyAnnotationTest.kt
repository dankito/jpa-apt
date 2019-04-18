package net.dankito.jpa.apt

import net.dankito.jpa.apt.util.AbstractProcessorTest
import org.junit.Test

class PropertyAnnotationTest : AbstractProcessorTest() {


    @Test
    fun booleanGetterIs() {
        val classNames = listOf("property_annotations/BooleanGetterIs.java")

        process(classNames)
    }

    @Test
    fun booleanGetterHas() {
        val classNames = listOf("property_annotations/BooleanGetterHas.java")

        process(classNames)
    }


    @Test
    fun enumString() {
        val classNames = listOf("property_annotations/EnumProperty.java")

        process(classNames)
    }

}