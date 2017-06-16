package net.dankito.jpa.apt.util

// Copied from https://github.com/querydsl/codegen/blob/master/src/main/java/com/mysema/codegen/SimpleCompiler.java

/*
 * Copyright 2010, Mysema Ltd
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

import java.io.*
import java.net.URL
import java.net.URLClassLoader
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.*
import java.util.jar.Manifest
import javax.lang.model.SourceVersion
import javax.tools.*

/**
 * SimpleCompiler provides a convenience wrapper of the JavaCompiler interface
 * with automatic classpath generation

 * @author tiwe
 */
class SimpleCompiler @JvmOverloads constructor(private val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler(), private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader) : JavaCompiler {

    private var classPath: String? = null

    private val classpath: String
        get() {
            var classPath = this.classPath

            if (classPath == null) {
                if (classLoader is URLClassLoader) {
                    classPath = getClassPath(classLoader)
                } else {
                    throw IllegalArgumentException("Unsupported ClassLoader " + classLoader)
                }
            }

            this.classPath = classPath
            return classPath
        }

    override fun getSourceVersions(): Set<SourceVersion> {
        return compiler.sourceVersions
    }

    override fun getStandardFileManager(
            diagnosticListener: DiagnosticListener<in JavaFileObject>, locale: Locale,
            charset: Charset): StandardJavaFileManager {
        return compiler.getStandardFileManager(diagnosticListener, locale, charset)
    }

    override fun getTask(out: Writer, fileManager: JavaFileManager,
                         diagnosticListener: DiagnosticListener<in JavaFileObject>,
                         options: Iterable<String>, classes: Iterable<String>,
                         compilationUnits: Iterable<JavaFileObject>): JavaCompiler.CompilationTask {
        return compiler.getTask(out, fileManager, diagnosticListener, options, classes,
                compilationUnits)
    }

    override fun isSupportedOption(option: String): Int {
        return compiler.isSupportedOption(option)
    }

    override fun run(inputStream: InputStream?, out: OutputStream, err: OutputStream, vararg arguments: String): Int {
        for (a in arguments) {
            if (a == "-classpath") {
                return compiler.run(inputStream, out, err, *arguments)
            }
        }

        // no classpath given
        val args = ArrayList<String>(arguments.size + 2)
        args.add("-classpath")
        args.add(classpath)
        for (arg in arguments) {
            args.add(arg)
        }
        return compiler.run(inputStream, out, err, *args.toTypedArray())
    }

    companion object {

        //    private static final Joiner pathJoiner = Joiner.on(File.pathSeparator);

        private fun isSureFireBooter(cl: URLClassLoader): Boolean {
            for (url in cl.urLs) {
                if (url.path.contains("surefirebooter")) {
                    return true
                }
            }

            return false
        }

        fun getClassPath(cl: URLClassLoader): String {
            try {
                val paths = ArrayList<String>()
                if (isSureFireBooter(cl)) {
                    // extract MANIFEST.MF Class-Path entry, since the Java Compiler doesn't handle
                    // manifest only jars in the classpath correctly
                    val url = cl.findResource("META-INF/MANIFEST.MF")
                    val manifest = Manifest(url.openStream())
                    val classpath = manifest.mainAttributes.getValue("Class-Path")
                    for (entry in classpath.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                        val entryUrl = URL(entry)
                        val decodedPath = URLDecoder.decode(entryUrl.path, "UTF-8")
                        paths.add(File(decodedPath).absolutePath)
                    }
                } else {
                    var classLoader: ClassLoader? = cl
                    while (classLoader is URLClassLoader) {
                        for (url in classLoader.urLs) {
                            val decodedPath = URLDecoder.decode(url.path, "UTF-8")
                            paths.add(File(decodedPath).absolutePath)
                        }
                        classLoader = classLoader.getParent()
                    }
                }
                //            return pathJoiner.join(paths);
                return joinPaths(paths)
            } catch (e: UnsupportedEncodingException) {
                //            throw new CodegenException(e);
                throw RuntimeException(e)
            } catch (e: IOException) {
                //            throw new CodegenException(e);
                throw RuntimeException(e)
            }

        }

        private fun joinPaths(paths: List<String>): String {
            var joinedPath = ""

            for (path in paths) {
                joinedPath += path + File.pathSeparator
            }

            if (joinedPath.length > 0) {
                joinedPath = joinedPath.substring(0, joinedPath.length - File.pathSeparator.length)
            }

            return joinedPath
        }
    }

}
