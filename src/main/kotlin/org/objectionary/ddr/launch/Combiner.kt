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
import org.objectionary.ddr.graph.CondAttributesSetter
import org.objectionary.ddr.graph.GraphBuilder
import org.objectionary.ddr.graph.InnerPropagator
import org.objectionary.ddr.graph.repr.Graph
import org.objectionary.ddr.transform.XslTransformer
import org.objectionary.ddr.transform.impl.BasicDecoratorsResolver
import org.objectionary.ddr.transform.impl.CondNodesResolver
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory

private val logger = LoggerFactory.getLogger("org.objectionary.ddr.launch.Combiner")
private val sep = File.separatorChar
val documents: MutableMap<Document, String> = mutableMapOf()

/**
 * Aggregates all steps of analysis
 *
 * @param path path to the directory to be analysed
 * @param dirPostfix postfix of the resulting directory
 */
fun launch(path: String, dirPostfix: String = "ddr") {
    documents.clear()
    val graph = buildGraph(path, false, dirPostfix)
    CondAttributesSetter(graph).processConditions()
    val attributesSetter = AttributesSetter(graph)
    attributesSetter.setAttributes()
    val innerPropagator = InnerPropagator(graph)
    innerPropagator.propagateInnerAttrs()
    CondNodesResolver(graph, documents).resolve()
    BasicDecoratorsResolver(graph, documents).resolve()
}

/**
 * Builds a single graph for all the files in the provided directory
 *
 * @param path path to the directory with files
 * @param gather if outputs should be gathered
 * @param dirPostfix postfix of the resulting directory
 * @return graph that was built
 */
fun buildGraph(
    path: String,
    gather: Boolean = true,
    dirPostfix: String = "ddr"
): Graph {
    val transformer = XslTransformer()
    val filePath = Path.of(path)
    Files.walk(filePath)
        .filter(Files::isRegularFile)
        .forEach {
            val tmpPath = createTempDirectories(filePath, it.toString(), gather, dirPostfix)
            transformer.transformXml(it.toString(), tmpPath)
            documents[getDocument(tmpPath)!!] = tmpPath
        }
    val builder = GraphBuilder(documents)
    builder.createGraph()
    return builder.graph
}

/**
 * Get Document from source xml file
 *
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

/**
 * Creates a new temporary directory for transformed xmir files
 *
 * @param path path to the directory with source xmir files or a single file
 * @param filename path to current xmir file in source directory
 * @param gather if outputs should be gathered
 * @param dirPostfix postfix of the resulting directory
 * @return path to the modified [filename] file in temporary directory
 */
private fun createTempDirectories(
    path: Path,
    filename: String,
    gather: Boolean = true,
    dirPostfix: String
): String {
    val strPath = path.toString()
    val tmpPath =
        if (gather) {
            "${strPath.substringBeforeLast(sep)}${sep}TMP$sep${strPath.substringAfterLast(sep)}_tmp${filename.substring(strPath.length)}"
        } else {
            "${strPath.substringBeforeLast(sep)}$sep${strPath.substringAfterLast(sep)}_$dirPostfix${filename.substring(strPath.length)}"
        }
    val forDirs = File(tmpPath.substringBeforeLast(sep)).toPath()
    Files.createDirectories(forDirs)
    val newFilePath = Path.of(tmpPath)
    try {
        Files.createFile(newFilePath)
    } catch (e: Exception) {
        logger.error(e.message)
    }
    return tmpPath
}
