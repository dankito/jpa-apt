package net.dankito.jpa.apt

import net.dankito.jpa.apt.util.AbstractProcessorTest
import org.junit.Test

class EntityAnnotationTest : AbstractProcessorTest() {


    @Test
    fun entityNameNotSet_TableNameEqualsClassName() {
        val classNames = listOf("class_annotations/EntityAnnotationNameUnset.java")

        process(classNames)
    }

    @Test
    fun entityNameSet_TableNameEqualsEntityName() {
        val classNames = listOf("class_annotations/EntityAnnotationNameSet.java")

        process(classNames)
    }

    @Test
    fun tableNameSet_TableNameEqualsTableAnnotationName() {
        val classNames = listOf("class_annotations/TableAnnotationNameSet.java")

        process(classNames)
    }

    @Test
    fun entityAndTableNameSet_TableNameEqualsTableAnnotationName() {
        val classNames = listOf("class_annotations/EntityAndTableAnnotationNameSet.java")

        process(classNames)
    }

    @Test
    fun prePersistAnnotationSet() {
        val classNames = listOf("lifecycle_methods/PrePersistLifecycleMethod.java")

        process(classNames)
    }

    @Test
    fun prePersistAnnotationWithMethodParametersSet() {
        val classNames = listOf("lifecycle_methods/PrePersistLifecycleMethodWithParameters.java")

        process(classNames)
    }

    @Test
    fun postPersistAnnotationSet() {
        val classNames = listOf("lifecycle_methods/PostPersistLifecycleMethod.java")

        process(classNames)
    }

    @Test
    fun postLoadnnotationSet() {
        val classNames = listOf("lifecycle_methods/PostLoadLifecycleMethod.java")

        process(classNames)
    }

    @Test
    fun preUpdateAnnotationSet() {
        val classNames = listOf("lifecycle_methods/PreUpdateLifecycleMethod.java")

        process(classNames)
    }

    @Test
    fun postUpdateAnnotationSet() {
        val classNames = listOf("lifecycle_methods/PostUpdateLifecycleMethod.java")

        process(classNames)
    }

    @Test
    fun preRemoveAnnotationSet() {
        val classNames = listOf("lifecycle_methods/PreRemoveLifecycleMethod.java")

        process(classNames)
    }

    @Test
    fun postRemoveAnnotationSet() {
        val classNames = listOf("lifecycle_methods/PostRemoveLifecycleMethod.java")

        process(classNames)
    }

}