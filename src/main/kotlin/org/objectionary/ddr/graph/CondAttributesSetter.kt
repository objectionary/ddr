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
            var tmpNode = node.firstChild.nextSibling
            var line = line(tmpNode)
            val cond: MutableList<Node> = mutableListOf(tmpNode)
            while (line(tmpNode.nextSibling.nextSibling) == line) {
                cond.add(tmpNode.nextSibling.nextSibling)
                tmpNode = tmpNode.nextSibling.nextSibling
                line = line(tmpNode)
            }
            tmpNode = tmpNode.nextSibling.nextSibling
            val fstOption: MutableList<Node> = mutableListOf(tmpNode)
            while (line(tmpNode.nextSibling.nextSibling) == line) {
                fstOption.add(tmpNode.nextSibling.nextSibling)
                tmpNode = tmpNode.nextSibling.nextSibling
                line = line(tmpNode)
            }
            tmpNode = tmpNode.nextSibling.nextSibling
            val sndOption: MutableList<Node> = mutableListOf(tmpNode)
            while (line(tmpNode.nextSibling.nextSibling) == line) {
                sndOption.add(tmpNode.nextSibling.nextSibling)
                tmpNode = tmpNode.nextSibling.nextSibling
                line = line(tmpNode)
            }
            graph.igCondNodes.add(IGraphCondNode(name(node)!!, cond, fstOption, sndOption))
        }
    }
}
