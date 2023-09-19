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

package org.objectionary.ddr.graph

import org.objectionary.ddr.graph.repr.Graph
import org.objectionary.ddr.graph.repr.IGraphNode
import org.objectionary.ddr.util.containsAttr
import org.objectionary.ddr.util.getAttrContent
import org.objectionary.ddr.util.packageName
import org.objectionary.ddr.util.toMutableList
import org.apache.commons.lang3.mutable.MutableBoolean
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.nio.file.Path

/**
 * Builds decoration hierarchy graph
 *
 * @property documents all XMIR documents
 */
class GraphBuilder(private val documents: MutableMap<Document, Path>) {
    /**
     * Graph of the program to be analysed
     */
    val graph = Graph()

    /**
     * Aggregates the process of graph creation:
     * Constructs inheritance graph, sets heads and leaves and processes cycles
     *
     * @return built [graph]
     */
    fun createGraph(): Graph {
        initializeGraph()
        constructInheritance()
        setLeaves()
        setHeads()
        return graph
    }

    /**
     * Initializes [graph] by adding all initial and abstract objects from the [documents]
     */
    fun initializeGraph() {
        documents.forEach {
            val objects = it.key.getElementsByTagName("o").toMutableList()
            val packageName = it.key.packageName()
            graph.igNodes.addAll(collectAbstracts(objects, packageName))
            graph.initialObjects.addAll(objects)
        }
    }

    /**
     * Puts all nodes representing abstract objects in a set of nodes [graph]
     */
    private fun constructInheritance() {
        graph.initialObjects.filter { isDecoration(it) }.forEach { decor ->
            val abstractBaseNode = getAbstractBaseObject(decor)
            abstractBaseNode?.let {
                val igChild = graph.igNodes.find { it.body == decor.parentNode }!!
                graph.connect(igChild, abstractBaseNode)
            }
        }
    }

    /**
     * Puts all nodes representing abstract objects in a set of nodes [graph]
     *
     * @param objects list of objects
     * @param packageName name of the package in which the described object is located
     */
    private fun collectAbstracts(objects: MutableList<Node>, packageName: String): MutableSet<IGraphNode> {
        val abstracts: MutableSet<IGraphNode> = mutableSetOf()
        objects.filter {
            it.containsAttr("abstract") && it.containsAttr("name")
        }.forEach { obj ->
            abstracts.add(IGraphNode(obj, packageName))
        }
        return abstracts
    }

    /**
     * Finds the definition of an abstract object, which is the object from "base" attribute of given decoration node
     *
     * @param node decoration node
     * @return found abstract base object of given decoration node or `null`
     */
    private fun getAbstractBaseObject(node: Node): IGraphNode? {
        val baseObjName = node.getAttrContent("base")
        val baseObjRef = node.getAttrContent("ref")
        return getAbstractViaRef(baseObjName, baseObjRef) ?: getAbstractViaPackage(baseObjName)
    }

    private fun getAbstractViaRef(
        baseName: String?,
        baseRef: String?
    ): IGraphNode? = graph.igNodes.find {
        it.body.getAttrContent("line") == baseRef && it.body.getAttrContent("name") == baseName
    }

    private fun getAbstractViaPackage(baseNodeName: String?): IGraphNode? {
        val packageName = baseNodeName?.substringBeforeLast('.')
        val nodeName = baseNodeName?.substringAfterLast('.')
        return graph.igNodes.find { it.name == nodeName && it.packageName == packageName }
    }

    /**
     * Check if given [node] is decoration statement
     *
     * @param node node to be inspected
     * @return `true` if [node] is decoration and `false` otherwise
     */
    private fun isDecoration(node: Node): Boolean {
        val name = node.getAttrContent("name") ?: return false
        return name == "@"
    }

    private fun setLeaves() =
        graph.igNodes.filter { it.children.isEmpty() }.forEach { graph.leaves.add(it) }

    /**
     * @todo #115:90m/DEV Current algorithm of finding graph heads is very inefficient and requires refactoring.
     *      To reimplement `setHeads()`, `findHeadsExcessively()` and `thinOutHeads()` functions.
     *      To reimplement all functions from `ClosedCycleProcessor.kt`.
     */
    private fun setHeads() {
        graph.leaves.forEach { findHeadsExcessively(it, mutableMapOf()) }
        val thinnedOutHeads: MutableSet<IGraphNode> = mutableSetOf()
        graph.heads.forEach {
            val found = MutableBoolean(false)
            thinOutHeads(it, thinnedOutHeads, mutableSetOf(), found)
            if (found.isFalse) {
                thinnedOutHeads.add(it)
            }
        }
        graph.heads.clear()
        thinnedOutHeads.forEach { graph.heads.add(it) }
        processClosedCycles(graph)
    }

    private fun findHeadsExcessively(
        node: IGraphNode,
        visited: MutableMap<IGraphNode, Boolean>
    ) {
        if (visited.containsKey(node) || node.parents.isEmpty()) {
            graph.heads.add(node)
        } else {
            visited[node] = true
            node.parents.forEach { findHeadsExcessively(it, visited) }
        }
    }

    private fun thinOutHeads(
        node: IGraphNode,
        toBeRemoved: MutableSet<IGraphNode>,
        visited: MutableSet<IGraphNode>,
        found: MutableBoolean
    ) {
        if (found.isTrue || toBeRemoved.contains(node)) {
            found.setTrue()
            return
        }
        if (visited.contains(node)) {
            toBeRemoved.add(node)
            found.setTrue()
            return
        }
        visited.add(node)
        node.children.forEach { thinOutHeads(it, toBeRemoved, visited, found) }
    }
}
