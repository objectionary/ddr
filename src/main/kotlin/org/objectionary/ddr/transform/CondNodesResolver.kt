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

import org.objectionary.ddr.graph.*
import org.objectionary.ddr.graph.repr.Graph
import org.objectionary.ddr.graph.repr.IGraphCondNode
import org.objectionary.ddr.graph.repr.IGraphNode
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.FileOutputStream

class CondNodesResolver(
    private val graph: Graph,
    private val documents: MutableMap<Document, String>
) {
    private val declarations: MutableMap<Node, Node?> = mutableMapOf()

    fun resolveCondNodes() {
        fff()
        documents.forEach { doc ->
            val outputStream = FileOutputStream(doc.value)
            outputStream.use { XslTransformer().writeXml(it, doc.key) }
        }
    }

    private fun fff() {
        val objects = graph.initialObjects
        val condNodes = graph.igNodes.filterIsInstance<IGraphCondNode>()
        condNodes.forEach { node ->
            objects.filter { ref(it) == line(node.body) }.forEach{insert(it, node)}
        }
    }

    private fun traverseDotChain(
        node: Node,
        abstract: IGraphNode
    ) {
        var sibling = node.nextSibling?.nextSibling
        while (base(sibling)?.startsWith(".") == true) {
            val base = base(sibling)
            val attr = abstract.attributes.find { it.name == base?.substring(1) }
            if (attr != null && sibling != null) {
//                insert(sibling, attr)
            }
            sibling = sibling?.nextSibling
        }
    }

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

    private fun insert(node: Node, igNode: IGraphCondNode) {
        val expr = collectDotChain(node)
//        val node = igNode.body.parentNode
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
        addDocumentChild(document, igNode, node, expr, parent)

        siblings.forEach { parent.appendChild(it) }
    }

    private fun addDocumentChild(
        document: Document,
        igNode: IGraphCondNode,
        node: Node,
        expr: MutableList<Node?>,
        parent: Node
    ) {
//        val node = igNode.body
        val ifChild: Element = document.createElement("o")
        ifChild.setAttribute("base", ".if")
        ifChild.setAttribute("line", line(node))
//        name(node)?.let { ifChild.setAttribute("name", it) }
        ifChild.setAttribute("pos", pos(node))
        igNode.cond.forEach { ifChild.appendChild(it.cloneNode(true)) }
        val ref1 = document.createAttribute("ref")
        ref1.value = ref(igNode.fstOption[0])
        val fstNode = node.cloneNode(true)
        fstNode.attributes.setNamedItem(ref1)
        ifChild.appendChild(fstNode)
        expr.forEach { ifChild.appendChild(it?.cloneNode(true)) }
        val ref2 = document.createAttribute("ref")
        ref2.value = ref(igNode.sndOption[0])
        val sndNode = node.cloneNode(true)
        sndNode.attributes.setNamedItem(ref2)
        ifChild.appendChild(sndNode)
        expr.forEach { ifChild.appendChild(it?.cloneNode(true)) }
        parent.appendChild(ifChild)
    }
}