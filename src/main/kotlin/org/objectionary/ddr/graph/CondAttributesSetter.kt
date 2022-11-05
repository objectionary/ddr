package org.objectionary.ddr.graph

import org.objectionary.ddr.graph.repr.Graph
import org.objectionary.ddr.graph.repr.IGraphCondAttr
import org.objectionary.ddr.graph.repr.IGraphCondNode
import org.objectionary.ddr.graph.repr.IgNodeCondition
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
        processApplications()
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

    private fun processApplications() {
        conditions.forEach { node ->
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
            val igCond = IgNodeCondition(cond)
            traverseParents(node.parentNode, igCond.freeVars)
            name(node)?.let {name ->
                if (name != "@") {
                    graph.igNodes.add(IGraphCondNode(node, packageName(node), igCond, fstOption, sndOption))
                    val parent = graph.igNodes.find { it.body == node.parentNode }
                    parent?.attributes?.add(IGraphCondAttr(name, 0, node, igCond, fstOption, sndOption))
                } else {
                    val parent = graph.igNodes.find { it.body == node.parentNode }
                    parent?.attributes?.add(IGraphCondAttr(name, 0, node, igCond, fstOption, sndOption))
                }
            }
        }
    }

    private fun traverseParents(node: Node, freeVars: MutableSet<String>) {
        abstract(node) ?: return
        var sibling = node.firstChild?.nextSibling
        while (base(sibling) == null && abstract(sibling) == null && sibling != null) {
            name(sibling)?.let { freeVars.add(it) }
            sibling = sibling?.nextSibling
        }
        traverseParents(node.parentNode, freeVars)
    }
}
