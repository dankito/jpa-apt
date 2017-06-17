package net.dankito.jpa.apt

import net.dankito.jpa.apt.util.AbstractProcessorTest
import org.junit.Test

class OneToOneAnnotationTest : AbstractProcessorTest() {

    companion object {
        private const val PackageName = "relationship_annotations/"
    }


    @Test
    fun unidirectional_JoinColumnNotSet() {
        val classNames = listOf(PackageName + "OneToOne_Unidirectional_OwningSide.java", PackageName + "OneToOne_Unidirectional_InverseSide.java")

        process(classNames)
    }

    @Test
    fun unidirectional_JoinColumnSet() {
        val classNames = listOf(PackageName + "OneToOne_Unidirectional_OwningSide_JoinColumnSet.java", PackageName + "OneToOne_Unidirectional_InverseSide.java")

        process(classNames)
    }


    @Test
    fun bidirectional() {
        val classNames = listOf(PackageName + "OneToOne_Bidirectional_OwningSide.java", PackageName + "OneToOne_Bidirectional_InverseSide.java")

        process(classNames)
    }

}