package org.polystat.eodv.graph

import org.w3c.dom.Node

class AttributesSetter(private val graph: Graph) {
    fun setSpecifiedAttributes() {
        graph.igNodes.forEach {
            val attributes = it.body.childNodes
            for (j in 0 until attributes.length) {
                val attr: Node = attributes.item(j)
                if (attr.attributes?.getNamedItem("abstract") != null) {
                    it.attributes.add(IGraphAttr(attr.attributes.getNamedItem("name").textContent, 0, attr))
                }
            }
        }
        println()
    }
}