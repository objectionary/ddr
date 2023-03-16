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
import org.objectionary.ddr.sources.SrsTransformed
import org.objectionary.ddr.transform.XslTransformer
import org.objectionary.ddr.transform.impl.BasicDecoratorsResolver
import org.objectionary.ddr.transform.impl.CondNodesResolver
import org.w3c.dom.Document

var documents: MutableMap<Document, String> = mutableMapOf()

/**
 * Aggregates all steps of analysis
 *
 * @param path path to the directory to be analysed
 * @param postfix postfix of the resulting directory
 */
fun launch(path: String, postfix: String = "ddr") {
    documents.clear()
    val graph = buildGraph(path, false, postfix)
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
 * @param postfix postfix of the resulting directory
 * @return graph that was built
 */
fun buildGraph(
    path: String,
    gather: Boolean = true,
    postfix: String = "ddr"
): Graph {
    val sources = SrsTransformed(path, XslTransformer(), postfix, gather)
    sources.walk()
    documents = sources.documents
    val builder = GraphBuilder(documents)
    builder.createGraph()
    return builder.graph
}
