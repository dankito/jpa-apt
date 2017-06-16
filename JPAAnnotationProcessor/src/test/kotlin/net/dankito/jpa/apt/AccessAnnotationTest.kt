package net.dankito.jpa.apt

import net.dankito.jpa.apt.util.AbstractProcessorTest
import org.junit.Test

class AccessAnnotationTest : AbstractProcessorTest() {

    @Test
    fun valueSetToField() {
        val classNames = listOf("class_annotations/AccessAnnotationSetToField.java")

        process(classNames)
    }

    @Test
    fun valueSetToProperty() {
        val classNames = listOf("class_annotations/AccessAnnotationSetToProperty.java")

        process(classNames)
    }


    /*      Access implicitly determined by placement of @Id annotation     */

    @Test
    fun accessAnnotationNotSet_IdAnnotationOnField() {
        val classNames = listOf("property_annotations/AccessAnnotationUnset_IdAnnotationOnField.java")

        process(classNames)
    }

    @Test
    fun accessAnnotationNotSet_IdAnnotationOnGetter() {
        val classNames = listOf("property_annotations/AccessAnnotationUnset_IdAnnotationOnGetter.java")

        process(classNames)
    }


    @Test
    fun accessAnnotationSetToProperty_IdAnnotationOnField() {
        val classNames = listOf("property_annotations/AccessAnnotationSetToProperty_IdAnnotationOnField.java")

        process(classNames)
    }

    @Test
    fun accessAnnotationSetToField_IdAnnotationOnGetter() {
        val classNames = listOf("property_annotations/AccessAnnotationSetToField_IdAnnotationOnGetter.java")

        process(classNames)
    }

}