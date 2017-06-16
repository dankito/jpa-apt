package net.dankito.jpa.apt

import net.dankito.jpa.apt.util.AbstractProcessorTest
import org.junit.Test

class ScalarPropertiesTest : AbstractProcessorTest() {

    companion object {
        private const val PackageName = "property_annotations/"
    }


    @Test
    fun readPrimitiveValues() {
        val classNames = listOf(PackageName + "EntityWithAllPrimitiveValues.java", "class_annotations/MappedSuperclass.java",
                "class_annotations/WithoutJavaxPersistenceAnnotations.java")

        process(classNames)
    }

}