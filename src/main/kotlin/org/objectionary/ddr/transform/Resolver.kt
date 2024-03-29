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

package org.objectionary.ddr.transform

import org.objectionary.ddr.graph.repr.Graph
import org.objectionary.ddr.graph.repr.IGraphNode
import org.objectionary.ddr.util.containsAttr
import org.objectionary.ddr.util.findRef
import org.objectionary.ddr.util.getAttr
import org.objectionary.ddr.util.getAttrContent
import org.objectionary.ddr.util.packageName
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.FileOutputStream
import java.nio.file.Path

/**
 * Abstract class for all resolvers
 */
abstract class Resolver(
    private val graph: Graph,
    private val documents: MutableMap<Document, Path>
) {
    /**
     * Map of objects and their declarations
     */
    protected val declarations: MutableMap<Node, Node?> = mutableMapOf()

    /**
     * Performs the resolution
     */
    abstract fun resolve()

    /**
     * Finds all objects that are declarations and puts them into a container
     */
    protected fun collectDeclarations() {
        val objects = graph.initialObjects
        for (node in objects) {
            val base = node.getAttrContent("base") ?: continue
            if (!node.containsAttr("abstract") && (!base.startsWith('.') || node.containsAttr("name"))) {
                declarations[node] = null
            }
        }
    }

    /**
     * Finds the abstract object for each declaration
     */
    protected fun resolveRefs() {
        declarations.keys.forEach { declarations[it] = findRef(it, graph.initialObjects, graph) }
    }

    /**
     * Applies transformer to each document in [documents]
     */
    protected fun transformDocuments() {
        documents.forEach { doc ->
            val outputStream = FileOutputStream(doc.value.toFile())
            outputStream.use { XslTransformer().writeXml(it, doc.key) }
        }
    }

    /**
     * Removes all siblings that go after [node]
     *
     * @param node to be modified,
     * so all of its sibling need to be removed and added after modification
     * @return list of removed siblings
     */
    protected fun removeSiblings(node: Node): MutableSet<Node> {
        val parent = node.parentNode
        val siblings = mutableSetOf(node)
        var tmpNode = node
        while (tmpNode.nextSibling != null) {
            siblings.add(tmpNode.nextSibling)
            tmpNode = tmpNode.nextSibling
        }
        siblings.forEach {
            parent.removeChild(it)
        }
        return siblings
    }

    /**
     * Gets abstract object for the given [node]
     *
     * @param node to find abstract for
     * @return found abstract or null if such abstract was not found
     */
    protected fun getIgAbstract(node: Node?): IGraphNode? {
        node.getAttr("abstract")?.let { return graph.igNodes.find { it.body == node } }
        val abstract = declarations[node]
        graph.igNodes.find { it.body == abstract }?.let { return it }
        val cand = findRef(node, graph.initialObjects, graph)
        return graph.igNodes.find { it.body == cand }
    }

    /**
     * Finds the first object the node references at
     *
     * @param node node to find ref for
     * @param objects set of potential references
     * @return found reference or null if such reference was not found
     */
    protected fun firstRef(
        node: Node,
        objects: MutableSet<Node>
    ): Node? {
        node.getAttrContent("ref")?.let { ref ->
            objects.forEach {
                if (it.getAttrContent("line") == ref && node.packageName() == it.packageName() && it.getAttrContent("name") == node.getAttrContent("base")) {
                    return lastInvocation(it)
                }
            }
        }
        node.getAttrContent("ref")?.let { ref ->
            objects.forEach {
                if (it.getAttrContent("line") == ref && node.packageName() == it.packageName()) {
                    return lastInvocation(it)
                }
            }
        }
        if (node.getAttrContent("base") == "^") {
            return node.parentNode?.parentNode
        }
        if (node.getAttrContent("base") == "$") {
            return node.parentNode
        }
        getAbstractViaPackage(node.getAttrContent("base"))?.body?.let { return it }
        val attrs = graph.igNodes.find { it.body == node.parentNode }?.attributes
        return attrs?.find { it.name == node.getAttrContent("base") }?.body
    }

    private fun lastInvocation(
        node: Node
    ): Node? {
        var res: Node? = node
        var sibling = node.nextSibling?.nextSibling
        while (sibling.getAttrContent("base")?.startsWith(".") == true) {
            res = sibling
            sibling = sibling?.nextSibling
            sibling?.attributes ?: run { sibling = sibling?.nextSibling }
        }
        return res
    }

    /**
     * Looks for the abstract object in other documents with the corresponding package names
     */
    private fun getAbstractViaPackage(baseNodeName: String?): IGraphNode? {
        val packageName = baseNodeName?.substringBeforeLast('.')
        val nodeName = baseNodeName?.substringAfterLast('.')
        return graph.igNodes.find { it.name.equals(nodeName) && it.packageName == packageName }
    }
}
