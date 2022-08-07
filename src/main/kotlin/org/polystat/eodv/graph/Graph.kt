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

    fun debugPrint(node: IGraphNode) {
        println(node)
        node.children.forEach {
            println("${node.body.nodeName} CHILD:")
            debugPrint(it)
        }
    }
}

data class IGraphNode(
    val body: Node
) {
    val children: MutableList<IGraphNode> = mutableListOf()
    val parents: MutableList<IGraphNode> = mutableListOf()
}