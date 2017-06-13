package net.dankito.jpa.apt

import net.dankito.jpa.apt.util.AbstractProcessorTest
import org.junit.Test

class EntityAnnotationTest : AbstractProcessorTest() {


    @Test
    fun nameNotSet_TableNameEqualsClassName() {
        val classNames = listOf("class_annotations/EntityAnnotationNameUnset.java")

        process(classNames)
    }

    @Test
    fun nameSet_TableNameEqualsEntityName() {
        val classNames = listOf("class_annotations/EntityAnnotationNameSet.java")

        process(classNames)
    }

}