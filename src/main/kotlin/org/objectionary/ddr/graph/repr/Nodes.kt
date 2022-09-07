package org.objectionary.ddr.graph.repr

import org.objectionary.ddr.graph.name
import org.w3c.dom.Node

open class IGraphNode(
    open val body: Node,
    open val packageName: String
) {
    /**
     * Name of the node
     */
    val name: String? = name(body)

    /**
     * Children of this node
     */
    val children: MutableSet<IGraphNode> = mutableSetOf()

    /**
     * Parents of this node
     */
    val parents: MutableSet<IGraphNode> = mutableSetOf()

    /**
     * List of attributes of this node (inner objects and propagated attributes)
     */
    val attributes: MutableList<IGraphAttr> = mutableListOf()
}

/**
 * Graph node representation. Alternative representation of EO object
 *
 * @property name name of the node
 * @property body represents the corresponding xml file node
 * @property packageName name of the package in which the described EO object is located
 */
@Suppress("CLASS_NAME_INCORRECT")
class IGraphBasicNode(
    override val body: Node,
    override val packageName: String
) : IGraphNode(body, packageName)

/**
 * Conditional graph attribute representation
 */
@Suppress("CLASS_NAME_INCORRECT")
class IGraphCondNode(
    override val body: Node,
    override val packageName: String,
    val cond: MutableList<Node>,
    val fstOption: MutableList<Node>,
    val sndOption: MutableList<Node>
) : IGraphNode(body, packageName)
