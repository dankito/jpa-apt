/*
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dankito.jpa.apt.util

import net.dankito.jpa.apt.JPAAnnotationProcessor
import org.junit.Assert
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import javax.annotation.processing.AbstractProcessor

abstract class AbstractProcessorTest {

    companion object {
        protected val packagePath = File("src/test/kotlin/net/dankito/jpa/apt/")

        protected val testEntitiesPath = File(packagePath, "test_entities")

        protected val OutputDirectoryName = "output"
    }


    private val compiler = SimpleCompiler()


    @Throws(IOException::class)
    protected fun process(classNames: List<String>) {
        val sourceFiles = createSourceFileList(*classNames.toTypedArray())

        process(JPAAnnotationProcessor::class.java, sourceFiles, OutputDirectoryName)
    }

    @Throws(IOException::class)
    protected fun process(processorClass: Class<out AbstractProcessor>, classes: List<String>, target: String) {
        val out = File("target/" + target)
        deleteFile(out)
        if (!out.mkdirs()) {
            //            Assert.fail("Creation of " + out.getPath() + " failed");
        }
        compile(processorClass, classes, target)
    }

    @Throws(IOException::class)
    protected fun compile(processorClass: Class<out AbstractProcessor>, classes: List<String>, target: String) {
        val options = ArrayList<String>(classes.size + 3)
        options.add("-s")
        options.add("target/" + target)
        options.add("-proc:only")
        options.add("-processor")
        options.add(processorClass.name)
        options.add("-sourcepath")
        options.add("src/test/java")
        options.addAll(aptOptions)
        options.addAll(classes)

        val out = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()
        val compilationResult = compiler.run(null, out, err, *options.toTypedArray())

        //        Processor.elementCache.clear();
        if (compilationResult != 0) {
            System.err.println(compiler.javaClass.name)
            Assert.fail("Compilation Failed:\n " + String(err.toByteArray(), Charset.forName("UTF-8")))
        }
    }

    protected val aptOptions: Collection<String>
        get() = emptyList()


    protected fun deleteFile(file: File) {
        if (file.isDirectory == false) {
            file.delete()
        } else {
            for (directoryEntry in file.listFiles()!!) {
                deleteFile(directoryEntry)
            }
        }
    }

    protected fun getFiles(path: String): List<String> {
        val classes = ArrayList<String>()
        for (file in File(path).listFiles()!!) {
            if (file.name.endsWith(".java")) {
                classes.add(file.path)
            } else if (file.isDirectory && !file.name.startsWith(".")) {
                classes.addAll(getFiles(file.absolutePath))
            }
        }
        return classes
    }

    protected fun createSourceFileList(vararg classNames: String): List<String> {
        return classNames.map { File(testEntitiesPath, it) }.map { it.path }
    }

}
