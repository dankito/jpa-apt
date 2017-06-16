package net.dankito.jpa.apt

import net.dankito.jpa.apt.util.AbstractProcessorTest
import org.junit.Test


class InheritanceTest : AbstractProcessorTest() {

    companion object {
        private const val PackageName = "inheritance_annotations/"
    }


    @Test
    fun nameNotSet_TableNameEqualsClassName() {
        val classNames = listOf(PackageName + "/Child_1_1.java", PackageName + "/EntityWithoutSuperclass.java", PackageName + "/Child_1_2.java",
                PackageName + "/Child_1_2.java", PackageName + "/MappedSuperclass.java")

        process(classNames)
    }

}