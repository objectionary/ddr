package org.objectionary.ddr.graph

import org.w3c.dom.Document

class CondAttributesSetter(
    private val graph: Graph,
    private val documents: MutableMap<Document, String>
) {
    private fun collectDeclarations() {
        val objects = graph.initialObjects
        for (node in objects) {
            val base = base(node) ?: continue
            if (abstract(node) == null && !base.startsWith('.')) {
//                declarations[node] = null
            }
        }
    }
}