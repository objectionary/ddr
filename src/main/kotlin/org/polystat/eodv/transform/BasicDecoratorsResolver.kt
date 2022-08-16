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

package org.polystat.eodv.transform

import org.polystat.eodv.graph.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource


class BasicDecoratorsResolver(
    private val graph: Graph,
    private val document: Document,
    private val outputStream: OutputStream
) {

    private val declarations: MutableMap<Node, Node?> = mutableMapOf()

    fun resolveDecorators() {
        collectDeclarations()
        resolveRefs()
        injectAttributes()
        outputStream.use { writeXml(it) }
    }

    private fun collectDeclarations() {
        val objects = document.getElementsByTagName("o")
        for (i in 0..objects.length) {
            val node = objects.item(i)
            val base = base(node) ?: continue
            if (abstract(node) == null && !base.startsWith('.')) {
                declarations[node] = null
            }
        }
    }

    private fun resolveRefs() {
        val objects = document.getElementsByTagName("o")
        declarations.keys.forEach { declarations[it] = findRef(it, objects) }
    }

    private fun injectAttributes() {
        val objects = document.getElementsByTagName("o")
        for (i in 0..objects.length) {
            val node = objects.item(i)
            val ref = ref(node) ?: continue // todo ^ doesn't have ref attr
            if (name(node) == null) {
                val baseObject = firstRef(ref, objects)
                val abstract = getIgAbstract(baseObject) ?: continue
                var sibling = node?.nextSibling?.nextSibling
                while (base(sibling)?.startsWith(".") == true) {
                    val base = base(sibling)
                    val attr = abstract.attributes.find { it.name == base?.substring(1) }
                    if (attr != null && sibling != null) insert(sibling, attr)
                    sibling = sibling?.nextSibling
                }
            }
        }
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
        // template: <o base=".@" line="13" method="" pos="5"/>
        for (i in 0 until attr.parentDistance) {
            val offset = 2 * i
            val newChild: Element = document.createElement("o")
            newChild.setAttribute("base", ".@")
            newChild.setAttribute("line", line(node))
//            newChild.setAttribute("method", "")
            newChild.setAttribute("pos", "${base(node)?.length?.plus(pos(node)?.toInt()!!)?.plus(offset)}")
            parent.appendChild(newChild)
        }
        siblings.forEach { parent.appendChild(it) }
    }

    private fun getIgAbstract(node: Node?): IGraphNode? {
        if (abstract(node) != null) return graph.igNodes.find { it.body == node }
        val abstract = declarations[node] ?: return null
        return graph.igNodes.find { it.body == abstract }
    }

    private fun firstRef(
        ref: String,
        objects: NodeList
    ): Node? {
        for (i in 0..objects.length) {
            val item = objects.item(i) ?: continue
            if (line(item) == ref) {
                return item
            }
        }
        return null
    }

    @Throws(TransformerException::class, UnsupportedEncodingException::class)
    private fun writeXml(output: OutputStream) {
        val sep = File.separator
        val prettyPrintXlst = "src${sep}main${sep}resources${sep}pretty_print.xslt"
        val transformer = TransformerFactory.newInstance().newTransformer(StreamSource(File(prettyPrintXlst)))
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no")
        val source = DOMSource(document)
        val result = StreamResult(output)
        transformer.transform(source, result)
    }
}