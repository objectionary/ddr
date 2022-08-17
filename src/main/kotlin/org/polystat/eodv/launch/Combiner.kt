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

package org.polystat.eodv.launch

import mu.KotlinLogging
import org.polystat.eodv.graph.AttributesSetter
import org.polystat.eodv.graph.Graph
import org.polystat.eodv.graph.GraphBuilder
import org.polystat.eodv.graph.InnerPropagator
import org.polystat.eodv.transform.BasicDecoratorsResolver
import org.polystat.eodv.transform.XslTransformer
import org.w3c.dom.Document
import org.xml.sax.SAXException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import kotlin.io.path.Path
import kotlin.io.path.pathString

private val logger = KotlinLogging.logger {}
private val sep = File.separatorChar
private val ADD_REFS_XSL = "src${sep}main${sep}resources${sep}add-refs.xsl"
private val RESOLVE_ALIASES = "src${sep}main${sep}resources${sep}resolve-aliases.xsl"
private val EXPAND_ALIASES = "src${sep}main${sep}resources${sep}expand-aliases.xsl"
val documents = mutableMapOf<Document, String>()

/**
 * @param path path to the directory to be analysed
 */
fun launch(path: String) {
    val graph = buildGraph(path)
    processAttributes(graph)
    val innerPropagator = InnerPropagator(graph)
    innerPropagator.propagateInnerAttrs()
    BasicDecoratorsResolver(graph, documents).resolveDecorators()
}

fun buildGraph(path: String): Graph {
    val transformer = XslTransformer()
    Files.walk(Paths.get(path))
        .filter(Files::isRegularFile)
        .forEach {
            try {
                val tmpPath = "${path.replace('/', sep)}_tmp${sep}tmp1$sep${it}"
                val forDirs = Path(tmpPath.substringBeforeLast(sep))
                Files.createDirectories(forDirs)
                val newFilePath = Paths.get(tmpPath)
                try {Files.createFile(newFilePath)} catch(ignored: Exception) {}
                transformer.createXsl(it.pathString, tmpPath, ADD_REFS_XSL)
//                documents[getDocument(tmpPath)!!] = tmpPath
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    Files.walk(Paths.get("${path.replace('/', sep)}_tmp${sep}tmp1"))
        .filter(Files::isRegularFile)
        .forEach {
            try {
                val tmpPath = it.toString().replace("tmp1", "tmp2")
                val forDirs = Path(tmpPath.substringBeforeLast(sep))
                Files.createDirectories(forDirs)
                val newFilePath = Paths.get(tmpPath)
                try {Files.createFile(newFilePath)} catch(ignored: Exception) {}
                transformer.createXsl(it.toString(), tmpPath, EXPAND_ALIASES)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    Files.walk(Paths.get("${path.replace('/', sep)}_tmp${sep}tmp2"))
        .filter(Files::isRegularFile)
        .forEach {
            try {
                val tmpPath = it.toString().replace("tmp2", "tmp3")
                val forDirs = Path(tmpPath.substringBeforeLast(sep))
                Files.createDirectories(forDirs)
                val newFilePath = Paths.get(tmpPath)
                try {Files.createFile(newFilePath)} catch(ignored: Exception) {}
                transformer.createXsl(it.toString(), tmpPath, RESOLVE_ALIASES)
                documents[getDocument(tmpPath)!!] = tmpPath
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    val builder = GraphBuilder(documents)
    builder.createGraph()
    return builder.graph
}

fun processAttributes(graph: Graph) {
    val attributesSetter = AttributesSetter(graph)
    attributesSetter.setDefaultAttributes()
    attributesSetter.pushAttributes()
}

fun getDocument(filename: String): Document? {
    try {
        val factory = DocumentBuilderFactory.newInstance()
        FileInputStream(filename).use { `is` ->
            return factory.newDocumentBuilder().parse(`is`)
        }
    } catch (e: Exception) {
        when (e) {
            is ParserConfigurationException, is SAXException ->
                logger.error { e.message }
            else -> throw e
        }
    }
    return null
}
