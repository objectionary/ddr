package org.polystat.eodv.graph

import org.w3c.dom.Node

class Graph {
    val igNodes: MutableSet<IGraphNode> = mutableSetOf()
    val heads: MutableSet<IGraphNode> = mutableSetOf()
    val leaves: MutableList<IGraphNode> = mutableListOf()

    fun connect(
        child: IGraphNode,
        parent: IGraphNode
    ) {
        child.parents.add(parent)
        parent.children.add(child)
    }
}

data class IGraphNode(
    val body: Node
) {
//    val name: String by lazy { body.attributes.getNamedItem("name").textContent }
    val name: String = body.attributes.getNamedItem("name").textContent // debug
    val children: MutableList<IGraphNode> = mutableListOf()
    val parents: MutableList<IGraphNode> = mutableListOf()
    val attributes: MutableList<IGraphAttr> = mutableListOf()
}

data class IGraphAttr(
    val name: String,
    val parentDistance: Int,
    val body: Node
)