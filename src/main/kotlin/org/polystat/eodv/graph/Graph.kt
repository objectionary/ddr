package org.polystat.eodv.graph

import org.w3c.dom.Node

/**
 * Decoration hierarchy graph representation
 */
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

/**
 * Graph node representation
 * @param body represents the corresponding xml file node
 */
data class IGraphNode(
    val body: Node
) {
//    val name: String by lazy { name(body) }
    val name: String? = name(body) // debug
    val children: MutableList<IGraphNode> = mutableListOf()
    val parents: MutableList<IGraphNode> = mutableListOf()
    val attributes: MutableList<IGraphAttr> = mutableListOf()
}

/**
 * Graph attribute representation
 * @param name is the name of the attribute
 * @param parentDistance is the distance to the parent, from which this attribute was pushed to current node
 * @param body represents the corresponding xml file node
 */
data class IGraphAttr(
    val name: String,
    val parentDistance: Int,
    val body: Node
)