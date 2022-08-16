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

package org.polystat.eodv.graph

import mu.KotlinLogging
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList

import org.xml.sax.SAXException
import java.io.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * Builds decoration hierarchy graph
 */
class GraphBuilder(private val document: Document) {
    private val logger = KotlinLogging.logger {}
    private val abstracts: MutableMap<String, MutableSet<Node>> = mutableMapOf()
    val graph = Graph()

    fun createGraph() {
        try {
            constructInheritance(document)
            setLeaves()
            graph.leaves.forEach { setHeads(it, mutableMapOf()) }
            val thinnedOutHeads: MutableSet<IGraphNode> = mutableSetOf()
            graph.heads.forEach {
                val found = mutableListOf(false)
                thinOutHeads(it, thinnedOutHeads, mutableSetOf(), found)
                if (!found[0]) {
                    thinnedOutHeads.add(it)
                }
            }
            graph.heads.clear()
            thinnedOutHeads.forEach { graph.heads.add(it) }
            processClosedCycles(graph)
        } catch (e: Exception) {
            when (e) {
                is IOException, is TransformerException ->
                    logger.error { e.message }
                else -> throw e
            }
        }
    }

    private fun abstracts(objects: NodeList) {
        for (i in 0..objects.length) {
            val node = objects.item(i)
            val name = name(node)
            if (abstract(node) != null && name != null) {
                abstracts.getOrPut(name) { mutableSetOf() }.add(node)
                graph.igNodes.add(IGraphNode(node))
            }
        }
    }

    private fun getAbstract(
        baseName: String?,
        baseRef: String?
    ): Node? {
        if (baseName != null && abstracts.contains(baseName)) {
            return abstracts[baseName]!!.find {
                line(it) == baseRef
            }
        }
        return null
    }

    private fun constructInheritance(document: Document) {
        val objects = document.getElementsByTagName("o")
        abstracts(objects)
        for (i in 0..objects.length) {
            val node = objects.item(i)
            val name = name(node)
            if (name != null && name == "@") {
                // check that @ attribute's base has an abstract object in this program
                val baseNodeName = base(node)
                val baseNodeRef = ref(node)
                val abstractBaseNode = getAbstract(baseNodeName, baseNodeRef)
                if (abstractBaseNode != null) {
                    val parentNode = node.parentNode
                    if (parentNode != null) {
                        var igChild = IGraphNode(node.parentNode)
                        val checkedChild = checkNodes(igChild)
                        if (checkedChild == null) {
                            graph.igNodes.add(igChild)
                        } else {
                            igChild = checkedChild
                        }
                        var igParent = IGraphNode(abstractBaseNode)
                        val checkedParent = checkNodes(igParent)
                        if (checkedParent == null) {
                            graph.igNodes.add(igParent)
                        } else {
                            igParent = checkedParent
                        }
                        graph.connect(igChild, igParent)
                    }
                }
            }
        }
    }

    private fun checkNodes(node: IGraphNode): IGraphNode? {
        return graph.igNodes.find { it.body.attributes == node.body.attributes }
    }

    private fun setLeaves() =
        graph.igNodes.filter { it.children.isEmpty() }.forEach { graph.leaves.add(it) }

    private fun setHeads(
        node: IGraphNode,
        visited: MutableMap<IGraphNode, Boolean>
    ) {
        if (visited.containsKey(node) || node.parents.isEmpty()) {
            graph.heads.add(node)
        } else {
            visited[node] = true
            node.parents.forEach { setHeads(it, visited) }
        }
    }

    private fun thinOutHeads(
        node: IGraphNode,
        toBeRemoved: MutableSet<IGraphNode>,
        visited: MutableSet<IGraphNode>,
        found: MutableList<Boolean>
    ) {
        if (found[0] || toBeRemoved.contains(node)) {
            found[0] = true
            return
        }
        if (visited.contains(node)) {
            toBeRemoved.add(node)
            found[0] = true
            return
        }
        visited.add(node)
        node.children.forEach { thinOutHeads(it, toBeRemoved, visited, found) }
    }

    private fun decorationCycles() {

    }
}
