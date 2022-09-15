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

import org.objectionary.ddr.graph.abstract
import org.objectionary.ddr.graph.base
import org.objectionary.ddr.graph.findRef
import org.objectionary.ddr.graph.line
import org.objectionary.ddr.graph.name
import org.objectionary.ddr.graph.packageName
import org.objectionary.ddr.graph.pos
import org.objectionary.ddr.graph.ref
import org.objectionary.ddr.graph.repr.Graph
import org.objectionary.ddr.graph.repr.IGraphAttr
import org.objectionary.ddr.graph.repr.IGraphCondNode
import org.objectionary.ddr.graph.repr.IGraphNode
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.FileOutputStream

/**
 * Collects all decorators and inserts desired .@ applications
 * @todo #43:30min document better
 */
class BasicDecoratorsResolver(
    private val graph: Graph,
    private val documents: MutableMap<Document, String>
) {
    private val declarations: MutableMap<Node, Node?> = mutableMapOf()

    /**
     * Aggregates process of resolving all decorators:
     * collects declarations, finds references of the decorators
     * and injects all needed .@ elements into the corresponding documents
     */
    fun resolveDecorators() {
        collectDeclarations()
        resolveRefs()
        injectAttributes()
        documents.forEach { doc ->
            val outputStream = FileOutputStream(doc.value)
            outputStream.use { XslTransformer().writeXml(it, doc.key) }
        }
    }

    private fun collectDeclarations() {
        val objects = graph.initialObjects
        for (node in objects) {
            val base = base(node) ?: continue
            if (abstract(node) == null && !base.startsWith('.')) {
                declarations[node] = null
            }
        }
    }

    private fun resolveRefs() =
        declarations.keys.forEach { declarations[it] = findRef(it, graph.initialObjects, graph) }

    @Suppress("AVOID_NULL_CHECKS")
    private fun injectAttributes() {
        val objects = graph.initialObjects
        for (node in objects) {
            val baseObject = firstRef(node, objects)
            val abstract = getIgAbstract(baseObject) ?: continue
            traverseDotChain(node, abstract)
            graph.igNodes.find { node.parentNode == it.body }?.let { parentNode ->
                val dist = parentNode.attributes.find { it.name == name(baseObject) }?.parentDistance
                if (dist != null) {
                    insertBefore(node, parentNode.body, dist)
                }
            }
        }
    }

    private fun traverseDotChain(
        node: Node,
        abstract: IGraphNode
    ) {
        var sibling = node.nextSibling?.nextSibling
//        sibling?.attributes?: run {
//            sibling = sibling?.nextSibling
//        }
        while (base(sibling)?.startsWith(".") == true) {
            val base = base(sibling)
            val attr = abstract.attributes.find { it.name == base?.substring(1) }
            if (attr != null && sibling != null) {
                insert(sibling, attr)
            }
            sibling = sibling?.nextSibling
        }
    }

    private fun insertBefore(
        node: Node,
        parent: Node,
        dist: Int
    ) {
        val siblings: MutableSet<Node> = mutableSetOf()
        var tmpNode = node
        while (tmpNode.nextSibling != null) {
            siblings.add(tmpNode.nextSibling)
            tmpNode = tmpNode.nextSibling
        }
        parent.removeChild(node)
        siblings.forEach { parent.removeChild(it) }
        val document = parent.ownerDocument
        if (dist > 0) {
            addDocumentChild(document, node, parent, "@", 0)
        }
        for (i in 1 until dist) {
            addDocumentChild(document, node, parent, ".@", i * 2)
        }
        val newChild: Element = document.createElement("o")
        for (i in 0 until node.attributes.length) {
            val attr = node.attributes.item(i)
            if (attr.nodeName == "base") {
                newChild.setAttribute("base", ".${attr.textContent}")
            } else {
                newChild.setAttribute(attr.nodeName, attr.textContent)
            }
        }
        parent.appendChild(newChild)
        siblings.forEach { parent.appendChild(it) }
    }

    private fun insert(node: Node, attr: IGraphAttr) {
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
        val document = parent.ownerDocument
        for (i in 0 until attr.parentDistance) {
            addDocumentChild(document, node, parent, ".@", i * 2)
        }
        siblings.forEach { parent.appendChild(it) }
    }

    private fun addDocumentChild(
        document: Document,
        node: Node,
        parent: Node,
        baseValue: String,
        offset: Int
    ) {
        val newChild: Element = document.createElement("o")
        newChild.setAttribute("base", baseValue)
        newChild.setAttribute("line", line(node))
        newChild.setAttribute("pos", "${base(node)?.length?.plus(pos(node)?.toInt()!!)?.plus(offset)}")
        parent.appendChild(newChild)
    }

    private fun getIgAbstract(node: Node?): IGraphNode? {
        abstract(node)?.let { return graph.igNodes.find { it.body == node } }
        val abstract = declarations[node] ?: return null
        return graph.igNodes.find { it.body == abstract }
    }

    private fun firstRef(
        node: Node,
        objects: MutableList<Node>
    ): Node? {
        ref(node)?.let { ref ->
            objects.forEach {
                if (line(it) == ref && packageName(node) == packageName(it)) {
                    return it
                }
            }
        }
        if (base(node) == "^") {
            return node.parentNode.parentNode
        }
        if (base(node) == "$") {
            return node.parentNode
        }
        getAbstractViaPackage(base(node))?.body?.let { return it }
        val attrs = graph.igNodes.find { it.body == node.parentNode }?.attributes
        return attrs?.find { it.name == base(node) }?.body
    }

    private fun getAbstractViaPackage(baseNodeName: String?): IGraphNode? {
        val packageName = baseNodeName?.substringBeforeLast('.')
        val nodeName = baseNodeName?.substringAfterLast('.')
        return graph.igNodes.find { it.name.equals(nodeName) && it.packageName == packageName }
    }
}
