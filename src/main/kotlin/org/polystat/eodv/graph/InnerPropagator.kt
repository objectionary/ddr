package org.polystat.eodv.graph

import org.w3c.dom.Document
import org.w3c.dom.Node

/**
 * Propagates inner attributes
 */
class InnerPropagator(
    private val document: Document,
    private val graph: Graph
) {
    private val decorators: MutableMap<IGraphNode, Boolean> = mutableMapOf()
    private val abstracts: MutableSet<IGraphNode> = mutableSetOf()

    fun collectDecoratorsAbstracts() {
        val objects = document.getElementsByTagName("o")
        for (i in 0..objects.length) {
            val node = objects.item(i)
            val name = node?.attributes?.getNamedItem("name")?.textContent
            if (name != null && name == "@") {
                decorators[IGraphNode(node)] = false
            }
//            if (node?.attributes?.getNamedItem("abstract") != null && name != null) {
//                abstracts.getOrPut(name) { mutableSetOf() }.add(node)
//            }
        }
    }

    fun collectAbstracts() {
        graph.igNodes.forEach {
            if (it.body.attributes?.getNamedItem("abstract") != null)
                abstracts.add(it)
        }
    }

    fun processDecorators() {
        while (decorators.containsValue(false)) {
            decorators.filter { !it.value }.forEach {
                getBaseAbstract(it.key)
            }
        }
    }

    private fun getBaseAbstract(key: IGraphNode) {

    }

}