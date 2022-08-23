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

import org.w3c.dom.Node

/**
 * Propagates inner attributes
 */
class InnerPropagator(
    private val graph: Graph
) {
    private val decorators: MutableMap<IGraphNode, Boolean> = mutableMapOf()
    private val abstracts: MutableMap<String, MutableSet<IGraphNode>> = mutableMapOf()

    fun propagateInnerAttrs() {
        collectDecorators()
        processDecorators()
    }

    private fun collectDecorators() {
        val objects = graph.initialObjects
        for (node in objects) {
            val name = name(node)
            if (name != null && name == "@") {
                decorators[IGraphNode(node, packageName(node))] = false
            }
            if (abstract(node) != null && name != null) {
                abstracts.getOrPut(name) { mutableSetOf() }.add(IGraphNode(node, packageName(node)))
            }
        }
    }

    private fun processDecorators() {
//        while (decorators.containsValue(false)) { // todo
        for (i in 0..5)
            decorators.filter { !it.value }.forEach {
                getBaseAbstract(it.key)
            }
//        }
    }

    private fun getBaseAbstract(key: IGraphNode) {
        var tmpKey = key.body
        while (base(tmpKey)?.startsWith('.') == true) {
            tmpKey = tmpKey.previousSibling.previousSibling
        }
        when (base(tmpKey)) {
            "^" -> {
                val abstract = tmpKey.parentNode.parentNode
                resolveAttrs(tmpKey, abstract, key)
            }
            "$" -> {} // todo
            else -> {
                val abstract = resolveRefs(tmpKey) ?: return
                resolveAttrs(tmpKey, abstract, key)
            }
        }
    }

    private fun resolveRefs(node: Node): Node? {
        return if (abstract(node) != null) {
            node
        } else {
            val objects = graph.initialObjects
            findRef(node, objects, graph)
        }
    }

    private fun resolveAttrs(node: Node, abstract: Node, key: IGraphNode): Boolean {
        var tmpAbstract = graph.igNodes.find { it.body == abstract } ?: return false
        var tmpNode: Node? = node.nextSibling.nextSibling ?: return false
        while (name(tmpAbstract.body) != base(key.body)?.substring(1)) {
            tmpAbstract = graph.igNodes.find { e ->
                tmpAbstract.attributes.find { base(tmpNode)?.substring(1) == name(it.body) }?.body == e.body
            } ?: return false
            tmpNode = tmpNode?.nextSibling?.nextSibling
        }
        val parent = node.parentNode ?: return false
        var igParent = graph.igNodes.find { it.body == parent }
        if (igParent == null) {
            graph.igNodes.add(IGraphNode(parent, packageName(parent)))
            igParent = graph.igNodes.find { it.body == parent }
        }
        tmpAbstract.attributes.forEach {
            if (igParent!!.attributes.find { a -> it.body == a.body } == null) {
                igParent.attributes.add(IGraphAttr(it.name, it.parentDistance + 1, it.body))
            }
        }
        graph.connect(igParent!!, tmpAbstract)
        return true
    }

}