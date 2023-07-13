/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Olesia Subbotina
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.objectionary.ddr.integration.resolver

import org.objectionary.ddr.TestBase
import org.objectionary.ddr.graph.AttributesSetter
import org.objectionary.ddr.graph.CondAttributesSetter
import org.objectionary.ddr.graph.GraphBuilder
import org.objectionary.ddr.graph.InnerPropagator
import org.objectionary.ddr.sources.SrsTransformed
import org.objectionary.ddr.transform.XslTransformer
import org.objectionary.ddr.transform.impl.BasicDecoratorsResolver
import org.objectionary.ddr.transform.impl.CondNodesResolver
import com.jcabi.xml.ClasspathSources
import com.jcabi.xml.XML
import com.jcabi.xml.XMLDocument
import com.jcabi.xml.XSLDocument
import org.apache.commons.io.FileUtils
import org.cactoos.io.OutputTo
import org.cactoos.io.ResourceOf
import org.eolang.parser.Syntax
import org.eolang.parser.XMIR
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Base class for testing decorators resolver
 *
 * @todo #121:60min ResolverBase test needs to be refactored. Some decomposition needs to be added into doTest method.
 */
open class ResolverBase : TestBase {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    private val postfix = "tmp"

    override fun doTest() {
        val path = getTestName()
        Files.walk(Paths.get(constructEoPath(path)))
            .filter(Files::isRegularFile)
            .forEach { eoToXMIR(it.toString()) }
        constructInPath(path)
        val documents = SrsTransformed(constructInPath(path), XslTransformer(), postfix).walk()
        val graph = GraphBuilder(documents).createGraph()
        CondAttributesSetter(graph).processConditions()
        AttributesSetter(graph).setAttributes()
        InnerPropagator(graph).propagateInnerAttrs()
        CondNodesResolver(graph, documents).resolve()
        BasicDecoratorsResolver(graph, documents).resolve()
        val cmpPath = constructCmpPath(path)
        Files.walk(Paths.get(cmpPath))
            .filter(Files::isRegularFile)
            .forEach {
                XslTransformer().singleTransformation(it.toString(), it.toString(), "/compress-aliases.xsl")
                XslTransformer().singleTransformation(
                    it.toString(),
                    it.toString(),
                    "/org/eolang/parser/wrap-method-calls.xsl"
                )
                XMIRToEo(it.toString(), path)
            }
        val outPath = constructOutPath(path)
        val cmpFiles: MutableList<String> = mutableListOf()
        Files.walk(Paths.get(outPath))
            .filter(Files::isRegularFile)
            .forEach { cmpFiles.add(it.toString()) }
        val eoOutPath = constructEoOutPath(path)
        Files.walk(Paths.get(eoOutPath))
            .filter(Files::isRegularFile)
            .forEach { file ->
                val actualBr: BufferedReader = File(file.toString()).bufferedReader()
                val actual = actualBr.use { it.readText() }
                val expectedFile = cmpFiles.find {
                    it.substringAfterLast(path) == file.toString().substringAfterLast(path)
                }
                val expectedBr: BufferedReader = File(expectedFile.toString()).bufferedReader()
                val expected = expectedBr.use { it.readText() }
                checkOutput(expected, actual)
            }
        try {
            val tmpDir =
                Paths.get("${constructInPath(path).replace('/', sep)}_$postfix").toString()
            FileUtils.deleteDirectory(File(tmpDir))
            FileUtils.deleteDirectory(File(Paths.get(constructInPath(path).substringBeforeLast(sep)).toString()))
            FileUtils.deleteDirectory(File(Paths.get(constructEoOutPath(path).substringBeforeLast(sep)).toString()))
        } catch (e: Exception) {
            logger.error(e.printStackTrace().toString())
        }
    }

    @Throws(Exception::class)
    @Suppress("FUNCTION_NAME_INCORRECT_CASE")
    private fun eoToXMIR(path: String) {
        val outFile = File(path.replaceFirst("${sep}in$sep", "${sep}xmirs$sep").replace(".eo", ".xmir"))
        Files.createDirectories(File(outFile.toPath().toString().substringBeforeLast(File.separator)).toPath())
        val syntax = Syntax(
            "transformer",
            ResourceOf(path.replace("src${sep}test${sep}resources$sep", "")),
            OutputTo(outFile.outputStream())
        )
        syntax.parse()
    }

    @Throws(Exception::class)
    @Suppress("FUNCTION_NAME_INCORRECT_CASE")
    private fun XMIRToEo(path: String, testName: String) {
        val outFile = File(
            path.replaceFirst(
                "xmirs$sep${testName}_$postfix",
                "eo_outputs$sep$testName"
            ).replace(".xmir", ".eo")
        )
        Files.createDirectories(File(outFile.toPath().toString().substringBeforeLast(File.separator)).toPath())
        val src = File(path).readText()
        val first: XML = clean(XMLDocument(src))
        val eolang = XMIR(first).toEO()
        outFile.outputStream().write(eolang.toByteArray())
    }

    private fun clean(xmir: XML): XML {
        val stripXml = File("src${sep}test${sep}resources${sep}resolver${sep}strip-xmir.xsl").inputStream()
        return XSLDocument(stripXml).with(ClasspathSources()).transform(xmir)
    }

    override fun constructOutPath(directoryName: String): String =
        "src${sep}test${sep}resources${sep}resolver${sep}out$sep$directoryName"

    override fun constructInPath(directoryName: String): String =
        "src${sep}test${sep}resources${sep}resolver${sep}xmirs$sep$directoryName"

    private fun constructEoOutPath(path: String): String =
        "src${sep}test${sep}resources${sep}resolver${sep}eo_outputs$sep$path"

    private fun constructEoPath(path: String): String =
        "src${sep}test${sep}resources${sep}resolver${sep}in$sep$path"

    private fun constructCmpPath(path: String) =
        "${constructInPath(path).substringBeforeLast(sep)}$sep${path}_$postfix"
}
