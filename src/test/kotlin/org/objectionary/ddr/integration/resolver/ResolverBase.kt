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

import org.objectionary.ddr.graph.AttributesSetter
import org.objectionary.ddr.graph.CondAttributesSetter
import org.objectionary.ddr.graph.GraphBuilder
import org.objectionary.ddr.graph.InnerPropagator
import org.objectionary.ddr.integration.IntegrationTestBase
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
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Base class for testing decorators resolver
 */
open class ResolverBase : IntegrationTestBase {
    override val logger = LoggerFactory.getLogger(this.javaClass.name)
    override val postfix = "tmp"
    private var testName: String = ""
    private val xmirInTmp = "xmirs_tmp"
    private val eoOutTmp = "eo_tmp"

    override fun doTest() {
        testName = getTestName()
        eoToXmir(constructEoInPath(testName))
        val sources = SrsTransformed(constructInPath(testName), XslTransformer(), postfix)
        val documents = sources.walk()
        val graph = GraphBuilder(documents).createGraph()
        CondAttributesSetter(graph).processConditions()
        AttributesSetter(graph).setAttributes()
        InnerPropagator(graph).propagateInnerAttrs()
        CondNodesResolver(graph, documents).resolve()
        BasicDecoratorsResolver(graph, documents).resolve()
        xmirToEo(constructResultPath(testName))
        Files.walk(Paths.get(constructOutPath(testName)))
            .filter(Files::isRegularFile)
            .forEach { file ->
                val expected = File(file.toString()).bufferedReader().use { it.readText().replace(" ", "") }
                val actualFilename = file.toString().replace("$eoOutTmp$sep", "out$sep")
                val actual = File(actualFilename).bufferedReader().use { it.readText().replace(" ", "") }
                checkOutput(expected, actual)
            }
        deleteTempDir(sources.inPath)
    }

    private fun eoToXmir(path: String) {
        Files.walk(Paths.get(path))
            .filter(Files::isRegularFile)
            .forEach { singleEoToXmir(it.toString()) }
    }

    private fun singleEoToXmir(path: String) {
        val outFile = File(path.replaceFirst("${sep}in$sep", "$sep$xmirInTmp$sep").replace(".eo", ".xmir"))
        Files.createDirectories(File(outFile.toPath().toString().substringBeforeLast(File.separator)).toPath())
        val syntax = Syntax(
            "transformer",
            ResourceOf(path.replace("src${sep}test${sep}resources$sep", "")),
            OutputTo(outFile.outputStream())
        )
        syntax.parse()
    }

    private fun xmirToEo(path: String) {
        Files.walk(Paths.get(path))
            .filter(Files::isRegularFile)
            .forEach {
                XslTransformer().singleTransformation(it.toString(), it.toString(), "/compress-aliases.xsl")
                XslTransformer().singleTransformation(
                    it.toString(),
                    it.toString(),
                    "/org/eolang/parser/wrap-method-calls.xsl"
                )
                singleXmirToEo(it.toString())
            }
    }

    private fun singleXmirToEo(path: String) {
        val outFile = File(
            path.replaceFirst(
                "$xmirInTmp$sep${testName}_$postfix",
                "$eoOutTmp$sep$testName"
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

    override fun deleteTempDir(pathToSource: Path) {
        val tmpDir = File("${pathToSource}_$postfix")
        try {
            FileUtils.deleteDirectory(tmpDir)
            FileUtils.deleteDirectory(File(constructInPath(testName).substringBeforeLast(sep)))
            FileUtils.deleteDirectory(File(constructEoOutPath(testName).substringBeforeLast(sep)))
        } catch (e: IOException) {
            logger.error(e.message)
            throw e
        }
    }

    override fun constructOutPath(directoryName: String): String =
        "src${sep}test${sep}resources${sep}resolver${sep}out$sep$directoryName"

    override fun constructInPath(directoryName: String): String =
        "src${sep}test${sep}resources${sep}resolver$sep$xmirInTmp$sep$directoryName"

    private fun constructEoOutPath(directoryName: String): String =
        "src${sep}test${sep}resources${sep}resolver$sep$eoOutTmp$sep$directoryName"

    private fun constructEoInPath(directoryName: String): String =
        "src${sep}test${sep}resources${sep}resolver${sep}in$sep$directoryName"
}
