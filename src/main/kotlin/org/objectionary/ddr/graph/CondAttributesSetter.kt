package org.objectionary.ddr.graph

import org.w3c.dom.Document
import org.w3c.dom.Node

class CondAttributesSetter(
    private val graph: Graph,
    private val documents: MutableMap<Document, String>
) {
    private val conditions : MutableSet<Node> = mutableSetOf()

    private fun collectConditions() {
        val objects = graph.initialObjects
        for (node in objects) {
            val base = base(node) ?: continue
            if (base == ".if") {
//                declarations[node] = null
            }
        }
    }
}