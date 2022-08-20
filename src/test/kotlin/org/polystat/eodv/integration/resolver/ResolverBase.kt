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

package org.polystat.eodv.integration.resolver

import com.jcabi.xml.*
import org.apache.commons.io.FileUtils
import org.cactoos.io.OutputTo
import org.cactoos.io.ResourceOf
import org.eolang.parser.Syntax
import org.eolang.parser.XMIR
import org.polystat.eodv.TestBase
import org.polystat.eodv.graph.InnerPropagator
import org.polystat.eodv.launch.buildGraph
import org.polystat.eodv.launch.documents
import org.polystat.eodv.launch.processAttributes
import org.polystat.eodv.transform.BasicDecoratorsResolver
import org.polystat.eodv.transform.XslTransformer
import java.io.BufferedReader
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path

open class ResolverBase : TestBase {

    override fun doTest() {
        val path = getTestName()
        documents.clear()
        Files.walk(Paths.get(constructEoPath(path)))
            .filter(Files::isRegularFile)
            .forEach { eoToXMIR(it.toString()) }
        val graph = buildGraph(constructInPath(path))
        processAttributes(graph)
        val innerPropagator = InnerPropagator(graph)
        innerPropagator.propagateInnerAttrs()
        BasicDecoratorsResolver(graph, documents).resolveDecorators()
        val cmpPath = constructCmpPath(path)
        Files.walk(Paths.get(cmpPath))
            .filter(Files::isRegularFile)
            .forEach {
                val compressAliases = "src${sep}main${sep}resources${sep}compress-aliases.xsl"
                XslTransformer().singleTransformation(it.toString(), it.toString(), compressAliases)
                val wrapMethodCalls = "src${sep}main${sep}resources${sep}wrap-method-calls.xsl"
                XslTransformer().singleTransformation(it.toString(), it.toString(), wrapMethodCalls)
                XMIRToEo(it.toString(), path)
            }
        val outPath = constructOutPath(path)
        val cmpFiles = mutableListOf<String>()
        Files.walk(Paths.get(outPath))
            .filter(Files::isRegularFile)
            .forEach {
                cmpFiles.add(it.toString())
            }
        val eoOutPath = constructEoOutPath(path)
        Files.walk(Paths.get(eoOutPath))
            .filter(Files::isRegularFile)
            .forEach {
                val actualBr: BufferedReader = File(it.toString()).bufferedReader()
                val actual = actualBr.use { br -> br.readText() }
                val expectedFile = cmpFiles.find { fn ->
                    fn.substringAfterLast(path) == it.toString().substringAfterLast(path)
                }
                val expectedBr: BufferedReader = File(expectedFile.toString()).bufferedReader()
                val expected = expectedBr.use { br -> br.readText() }
                checkOutput(actual, expected)
            }
        val tmpDir =
            Paths.get("${constructInPath(path).replace('/', sep).substringBeforeLast(sep)}${sep}TMP").toString()
        FileUtils.deleteDirectory(File(tmpDir))
    }

    @Throws(Exception::class)
    private fun eoToXMIR(path: String) {
        val outFile = File(path.replaceFirst("eo_sources", "in").replace(".eo", ".xmir"))
        Files.createDirectories(Path(outFile.toPath().toString().substringBeforeLast(File.separator)))
        val syntax = Syntax(
            "transformer",
            ResourceOf(path.replace("src${sep}test${sep}resources${sep}", "")),
            OutputTo(outFile.outputStream())
        )
        syntax.parse()
    }

    @Throws(Exception::class)
    private fun XMIRToEo(path: String, testName: String) {
        val outFile = File(
            path.replaceFirst("in${sep}TMP${sep}${testName}_tmp",
                "eo_outputs${sep}$testName").replace(".xmir", ".eo")
        )
        Files.createDirectories(Path(outFile.toPath().toString().substringBeforeLast(File.separator)))
        val src = File(path).readText()
        val first: XML = clean(XMLDocument(src))
        val eolang = XMIR(first).toEO()
        outFile.outputStream().write(eolang.toByteArray())
    }

    private fun clean(xmir: XML): XML {
        val stripXml = File("src/test/resources/integration/strip-xmir.xsl").inputStream()
        return XSLDocument(stripXml).with(ClasspathSources()).transform(xmir)
    }

    override fun constructOutPath(path: String): String =
        "src${sep}test${sep}resources${sep}integration${sep}out${sep}$path"

    override fun constructInPath(path: String): String =
        "src${sep}test${sep}resources${sep}integration${sep}in${sep}$path"

    private fun constructEoOutPath(path: String): String =
        "src${sep}test${sep}resources${sep}integration${sep}eo_outputs${sep}$path"

    private fun constructEoPath(path: String): String =
        "src${sep}test${sep}resources${sep}integration${sep}eo_sources${sep}$path"

    private fun constructCmpPath(path: String) =
        "${constructInPath(path).substringBeforeLast(sep)}${sep}TMP${sep}${path}_tmp"
}