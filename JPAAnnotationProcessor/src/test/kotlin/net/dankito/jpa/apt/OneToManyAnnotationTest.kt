package net.dankito.jpa.apt

import net.dankito.jpa.apt.util.AbstractProcessorTest
import org.junit.Test

class OneToManyAnnotationTest : AbstractProcessorTest() {

    companion object {
        private const val PackageName = "relationship_annotations/"
    }


    @Test
    fun unidirectional_JoinColumnNotSet() {
        val classNames = listOf(PackageName + "OneToMany_Unidirectional_OwningSide.java", PackageName + "OneToMany_Unidirectional_InverseSide.java")

        process(classNames)
    }


    @Test
    fun bidirectional() {
        val classNames = listOf(PackageName + "OneToMany_Bidirectional_OwningSide.java", PackageName + "OneToMany_Bidirectional_InverseSide.java")

        process(classNames)
    }

}