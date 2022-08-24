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

typealias Abstracts = MutableMap<String, MutableSet<IGraphNode>>

/**
 * Propagates inner attributes
 */
class InnerPropagator(
    private val graph: Graph
) {
    private val decorators: MutableMap<IGraphNode, Boolean> = mutableMapOf()
    private val abstracts: Abstracts = mutableMapOf()

    /**
     * Propagates attributes of objects that are defined not in the global scope, but inside other objects
     */
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

    @Suppress("MAGIC_NUMBER")
    private fun processDecorators() {
        val repetitions = 5
        // while (decorators.containsValue(false)) { // todo
        for (i in 0..repetitions) {
            decorators.filter { !it.value }.forEach {
                getBaseAbstract(it.key)
            }
        }
        // }
    }

    /**
     * @return abstract object corresponding to the base attribute of the [key] node
     */
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
            // todo handle
            "$" -> {}
            else -> {
                val abstract = resolveRefs(tmpKey) ?: return
                resolveAttrs(tmpKey, abstract, key)
            }
        }
    }

    /**
     * Finds an actual definition of an object that was copied into given [node]
     */
    private fun resolveRefs(node: Node): Node? {
        abstract(node)?.let { return node }
        return findRef(node, graph.initialObjects, graph)
    }

    /**
     * Goes back through the chain of dot notations and propagates
     * all the attributes of the applied node into the current object
     */
    private fun resolveAttrs(
        node: Node,
        abstract: Node,
        key: IGraphNode
    ) {
        var tmpAbstract = graph.igNodes.find { it.body == abstract } ?: return
        var tmpNode: Node? = node.nextSibling.nextSibling ?: return
        while (name(tmpAbstract.body) != base(key.body)?.substring(1)) {
            tmpAbstract = graph.igNodes.find { graphNode ->
                tmpAbstract.attributes.find {
                    base(tmpNode)?.substring(1) == name(it.body)
                }?.body == graphNode.body
            } ?: return
            tmpNode = tmpNode?.nextSibling?.nextSibling
        }
        val parent = node.parentNode ?: return
        graph.igNodes.find { it.body == parent } ?: run { graph.igNodes.add(IGraphNode(parent, packageName(parent))) }
        val igParent = graph.igNodes.find { it.body == parent } ?: return
        tmpAbstract.attributes.forEach { graphNode ->
            igParent.attributes.find { graphNode.body == it.body }
                ?: igParent.attributes.add(IGraphAttr(graphNode.name, graphNode.parentDistance + 1, graphNode.body))
        }
        graph.connect(igParent, tmpAbstract)
        return
    }
}
