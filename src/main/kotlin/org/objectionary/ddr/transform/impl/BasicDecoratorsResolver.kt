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

package org.objectionary.ddr.transform.impl

import org.objectionary.ddr.graph.repr.Graph
import org.objectionary.ddr.graph.repr.IGraphAttr
import org.objectionary.ddr.graph.repr.IGraphNode
import org.objectionary.ddr.transform.Resolver
import org.objectionary.ddr.util.getAttrContent
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.nio.file.Path

/**
 * Collects all decorators and inserts desired .@ applications
 */
class BasicDecoratorsResolver(
    private val graph: Graph,
    documents: MutableMap<Document, Path>
) : Resolver(graph, documents) {
    /**
     * Aggregates process of resolving all decorators:
     * collects declarations, finds references of the decorators
     * and injects all needed .@ elements into the corresponding documents
     */
    override fun resolve() {
        collectDeclarations()
        resolveRefs()
        injectAttributes()
        transformDocuments()
    }

    /**
     * Traverses objects and injects phi attributes for each.
     * Plus, checks if phi attribute has to be inserted before the object
     */
    private fun injectAttributes() {
        val objects = graph.initialObjects
        for (node in objects) {
            val baseObject = firstRef(node, objects)
            val abstract = getIgAbstract(baseObject) ?: continue
            traverseDotChain(node, abstract)
            graph.igNodes.find { node.parentNode == it.body }?.let { parentNode ->
                parentNode.attributes
                    .find { it.name == baseObject.getAttrContent("name") }?.parentDistance
                    ?.let { insertBefore(node, parentNode.body, it) }
            }
        }
    }

    /**
     * Traverses dot chain and inserts phi attributes along the way
     */
    private fun traverseDotChain(
        node: Node,
        abstract: IGraphNode
    ) {
        var sibling = node.nextSibling
        sibling?.attributes ?: run {
            sibling = sibling?.nextSibling
        }
        while (sibling.getAttrContent("base")?.startsWith(".") == true) {
            val base = sibling.getAttrContent("base")
            val attr = abstract.attributes.find { it.name == base?.substring(1) }
            if (attr != null && sibling != null) {
                insert(sibling, attr)
            }
            sibling = sibling?.nextSibling
        }
    }

    /**
     * Inserts phi before the object like this: @.foo
     */
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

    /**
     * Inserts phi attributes in between applications like this: foo.@.bar
     */
    private fun insert(node: Node, attr: IGraphAttr) {
        val parent = node.parentNode
        val siblings = removeSiblings(node)
        val document = parent.ownerDocument
        for (i in 0 until attr.parentDistance) {
            addDocumentChild(document, node, parent, ".@", i * 2)
        }
        siblings.forEach { parent.appendChild(it) }
    }

    /**
     * Constructs phi attribute node to be inserted
     */
    private fun addDocumentChild(
        document: Document,
        node: Node,
        parent: Node,
        baseValue: String,
        offset: Int
    ) {
        val newChild: Element = document.createElement("o")
        newChild.setAttribute("base", baseValue)
        newChild.setAttribute("line", node.getAttrContent("line"))
        if (baseValue == ".@") {
            newChild.setAttribute("method", "")
        }
        newChild.setAttribute("pos", "${node.getAttrContent("base")?.length
            ?.plus(node.getAttrContent("pos")?.toInt()!!)?.plus(offset)}")
        parent.appendChild(newChild)
    }
}
