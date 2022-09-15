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

import org.objectionary.ddr.graph.base
import org.objectionary.ddr.graph.line
import org.objectionary.ddr.graph.name
import org.objectionary.ddr.graph.pos
import org.objectionary.ddr.graph.ref
import org.objectionary.ddr.graph.repr.Graph
import org.objectionary.ddr.graph.repr.IGraphCondNode
import org.objectionary.ddr.graph.repr.IGraphNode
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Conditional nodes resolver
 */
class CondNodesResolver(
    private val graph: Graph,
    private val documents: MutableMap<Document, String>
) : Resolver(documents) {
    /**
     * Aggregate process of conditional nodes resolving
     */
    override fun resolve() {
        processObjects()
        documents.forEach {
            val objects: MutableList<Node> = mutableListOf()
            val docObjects = it.key.getElementsByTagName("o")
            for (i in 0 until docObjects.length) {
                objects.add(docObjects.item(i))
            }
            graph.initialObjects.addAll(objects)
        }
        transformDocuments()
    }

    /**
     * Processes only the usages of cond nodes
     */
    private fun processObjects() {
        val objects = graph.initialObjects
        val condNodes: List<IGraphCondNode> = graph.igNodes.filterIsInstance(IGraphCondNode::class.java)
        condNodes.forEach { node ->
            objects.filter { ref(it) == line(node.body) }.forEach { insert(it, node) }
        }
    }

    // @todo #39:30min this method should be used
    private fun traverseDotChain(
        node: Node,
        abstract: IGraphNode
    ) {
        var sibling = node.nextSibling?.nextSibling
        while (base(sibling)?.startsWith(".") == true) {
            val base = base(sibling)
            val attr = abstract.attributes.find { it.name == base?.substring(1) }
            if (attr != null && sibling != null) {
                // insert(sibling, attr)
            }
            sibling = sibling?.nextSibling
        }
    }

    /**
     * Collects the whole expression on which the cond node participates
     */
    private fun collectDotChain(
        node: Node
    ): MutableList<Node?> {
        val res: MutableList<Node?> = mutableListOf()
        var sibling = node.nextSibling?.nextSibling
        while (base(sibling)?.startsWith(".") == true) {
            res.add(sibling)
            sibling = sibling?.nextSibling
        }
        return res
    }

    /**
     * Inserts an if block
     */
    private fun insert(node: Node, igNode: IGraphCondNode) {
        val expr = collectDotChain(node)
        val parent = node.parentNode
        val siblings = removeSiblings(node)
        val document = parent.ownerDocument
        val child = addDocumentChild(document, igNode, node, expr)
        parent.appendChild(child)
        siblings.forEach { parent.appendChild(it) }
        parent.removeChild(node)
        expr.forEach { parent.removeChild(it) }
    }

    /**
     * Creates in if block
     *
     * @param igNode is a node that corresponds to conditional node
     * where the node object was created
     * @param expr is the whole dot chain that follows processed node
     */
    private fun addDocumentChild(
        document: Document,
        igNode: IGraphCondNode,
        node: Node,
        expr: MutableList<Node?>
    ): Element {
        val ifChild: Element = document.createElement("o")
        val phi = expr.any { name(it) == "@" }
        ifChild.setAttribute("base", ".if")
        ifChild.setAttribute("line", line(node))
        ifChild.setAttribute("pos", pos(node))
        if (phi) {
            ifChild.setAttribute("name", "@")
            expr.find { name(it) == "@" }?.attributes?.removeNamedItem("name")
        }
        igNode.cond.forEach { ifChild.appendChild(it.cloneNode(true)) }
        appendExpr(document, node, expr, ifChild, igNode.fstOption[0])
        appendExpr(document, node, expr, ifChild, igNode.sndOption[0])
        return ifChild
    }

    /**
     * Appends the whole dot chain to [ifChild]
     */
    private fun appendExpr(
        document: Document,
        node: Node,
        expr: MutableList<Node?>,
        ifChild: Node,
        refOption: Node
    ) {
        val ref = document.createAttribute("ref").apply { value = ref(refOption) }
        val clonedNode = node.cloneNode(true).apply { attributes.setNamedItem(ref) }
        ifChild.appendChild(clonedNode)
        expr.forEach { ifChild.appendChild(it?.cloneNode(true)) }
    }
}
