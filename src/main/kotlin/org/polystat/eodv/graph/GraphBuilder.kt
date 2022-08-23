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

import java.io.*
import javax.xml.transform.TransformerException

/**
 * Builds decoration hierarchy graph
 */
class GraphBuilder(private val documents: MutableMap<Document, String>) {
    private val logger = KotlinLogging.logger {}
    private val abstracts: MutableMap<String, MutableSet<Node>> = mutableMapOf()
    val graph = Graph()

    fun createGraph() {
        try {
            constructInheritance()
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

    private fun abstracts(objects: MutableList<Node>, packageName: String) =
        objects.forEach {
            val name = name(it)
            if (abstract(it) != null && name != null) {
                abstracts.getOrPut(name) { mutableSetOf() }.add(it)
                graph.igNodes.add(IGraphNode(it, packageName))
            }
        }

    private fun getAbstractViaRef(
        baseName: String?,
        baseRef: String?
    ): Node? =
        if (baseName != null && abstracts.contains(baseName)) {
            abstracts[baseName]!!.find {
                line(it) == baseRef
            }
        } else null

    private fun getAbstractViaPackage(baseNodeName: String?): IGraphNode? { // todo может быть там что-то импортится из пакета и несколько методов вызыватеся, тогда так отсекать последнюю точку плохо
        val packageName = baseNodeName?.substringBeforeLast('.')
        val nodeName = baseNodeName?.substringAfterLast('.')
        return graph.igNodes.find { it.name.equals(nodeName) && it.packageName == packageName }
    }

    private fun constructInheritance() {
        documents.forEach {
            val objects = mutableListOf<Node>()
            val docObjects = it.key.getElementsByTagName("o")
            val packageName = packageName(docObjects.item(0))
            for (i in 0 until docObjects.length) {
                objects.add(docObjects.item(i))
            }
            abstracts(objects, packageName)
            graph.initialObjects.addAll(objects)
        }
        for (node in graph.initialObjects) {
            val name = name(node)
            if (name != null && name == "@") {
                // check that @ attribute's base has an abstract object in this program
                val baseNodeName = base(node)
                val baseNodeRef = ref(node)
                var abstractBaseNode = getAbstractViaRef(baseNodeName, baseNodeRef)
                if (abstractBaseNode == null) {
                    abstractBaseNode = getAbstractViaPackage(baseNodeName)?.body
                }
                if (abstractBaseNode != null) {
                    val parentNode = node.parentNode
                    if (parentNode != null) {
                        var igChild = IGraphNode(node.parentNode, packageName(node.parentNode))
                        val checkedChild = checkNodes(igChild)
                        if (checkedChild == null) {
                            graph.igNodes.add(igChild)
                        } else {
                            igChild = checkedChild
                        }
                        var igParent = IGraphNode(abstractBaseNode, packageName(abstractBaseNode))
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
