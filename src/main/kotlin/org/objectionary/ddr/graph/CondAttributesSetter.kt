package org.objectionary.ddr.graph

import org.w3c.dom.Node

/**
 * Class for processing conditional attributes
 *
 * @property graph graph of the program
 */
class CondAttributesSetter(
    private val graph: Graph
) {
    private val conditions: MutableSet<Node> = mutableSetOf()

    /**
     * Aggregates the process of processing conditional attributes
     */
    fun processConditions() {
        collectConditions()
        processApplicationsToNames()
    }

    private fun collectConditions() {
        val objects = graph.initialObjects
        for (node in objects) {
            val base = base(node) ?: continue
            if (base == ".if") {
                conditions.add(node)
            }
        }
    }

    private fun processApplicationsToNames() {
        conditions.filter { name(it) != "@" }.forEach { node ->
            val cond: MutableList<Node> = mutableListOf()
            var tmpNode = node
            var line = line(tmpNode.nextSibling)
            cond.add(tmpNode.nextSibling)
            while (line(tmpNode.nextSibling) == line) {
                cond.add(tmpNode.nextSibling)
                tmpNode.nextSibling
                line = line(tmpNode)
            }
        }
    }
}
