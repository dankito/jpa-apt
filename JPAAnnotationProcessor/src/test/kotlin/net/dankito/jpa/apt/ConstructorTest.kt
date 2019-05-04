package net.dankito.jpa.apt

import net.dankito.jpa.apt.util.AbstractProcessorTest
import org.junit.Test

class ConstructorTest : AbstractProcessorTest() {

    companion object {
        private const val PackageName = "class_attributes/"
    }


    @Test
    fun entityWithNoArgConstructor() {
        val classNames = listOf(PackageName + "EntityWithNoArgConstructor.java")

        process(classNames)
    }

    @Test(expected = AssertionError::class)
    fun entityWithoutNoArgConstructor() {
        val classNames = listOf(PackageName + "EntityWithoutNoArgConstructor.java") // a class with no no-arg constructor shall not compile

        process(classNames)
    }

}