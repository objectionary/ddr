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

package org.objectionary.ddr.launch

import org.objectionary.ddr.graph.AttributesSetter
import org.objectionary.ddr.graph.Graph
import org.objectionary.ddr.graph.GraphBuilder
import org.objectionary.ddr.graph.InnerPropagator
import org.objectionary.ddr.transform.BasicDecoratorsResolver
import org.objectionary.ddr.transform.XslTransformer
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilderFactory

private val logger = LoggerFactory.getLogger("org.objectionary.ddr.launch.Combiner")
private val sep = File.separatorChar
val documents: MutableMap<Document, String> = mutableMapOf()

/**
 * Aggregates all steps of analysis
 *
 * @param path path to the directory to be analysed
 */
fun launch(path: String) {
    documents.clear()
    val graph = buildGraph(path, false)
    processAttributes(graph)
    val innerPropagator = InnerPropagator(graph)
    innerPropagator.propagateInnerAttrs()
    BasicDecoratorsResolver(graph, documents).resolveDecorators()
}

/**
 * Builds a single graph for all the files in the provided directory
 *
 * @param path path to the directory with files
 * @param gather if outputs should be gathered
 * @return graph that was built
 */
fun buildGraph(path: String, gather: Boolean = true): Graph {
    val transformer = XslTransformer()
    Files.walk(Paths.get(path))
        .filter(Files::isRegularFile)
        .forEach {
            val tmpPath = createTempDirectories(path, it.toString(), gather)
            transformer.transformXml(it.toString(), tmpPath)
            documents[getDocument(tmpPath)!!] = tmpPath
        }
    val builder = GraphBuilder(documents)
    builder.createGraph()
    return builder.graph
}

/**
 * Aggregates attributes propagation
 *
 * @param graph graph, whose nodes' attributes will be processed
 */
fun processAttributes(graph: Graph) {
    val attributesSetter = AttributesSetter(graph)
    attributesSetter.setDefaultAttributes()
    attributesSetter.pushAttributes()
}

/**
 * @param filename source xml filename
 * @return generated Document
 */
fun getDocument(filename: String): Document? {
    try {
        val factory = DocumentBuilderFactory.newInstance()
        FileInputStream(filename).use { return factory.newDocumentBuilder().parse(it) }
    } catch (e: Exception) {
        logger.error(e.printStackTrace().toString())
    }
    return null
}

private fun createTempDirectories(
    path: String,
    filename: String,
    gather: Boolean = true
): String {
    val tmpPath =
        if (gather) {
            "${path.substringBeforeLast(sep)}${sep}TMP$sep${path.substringAfterLast(sep)}_tmp${filename.substring(path.length)}"
        } else {
            "${path.substringBeforeLast(sep)}$sep${path.substringAfterLast(sep)}_ddr${filename.substring(path.length)}"
        }
    val forDirs = File(tmpPath.substringBeforeLast(sep)).toPath()
    Files.createDirectories(forDirs)
    val newFilePath = Paths.get(tmpPath)
    try {
        Files.createFile(newFilePath)
    } catch (e: Exception) {
        logger.error(e.message)
    }
    return tmpPath
}
