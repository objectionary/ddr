package org.objectionary.ddr.graph.repr

import org.objectionary.ddr.graph.name
import org.w3c.dom.Node

/**
 * Common class for all node types
 *
 * @property body represents the corresponding xml file node
 * @property packageName name of the package in which the described EO object is located
 */
@Suppress("CLASS_NAME_INCORRECT")
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
 * Conditional graph node representation
 *
 * @property body represents the corresponding xml file node
 * @property packageName name of the package in which the described EO object is located
 * @property cond list of nodes representing the condition
 * @property fstOption list of nodes representing the option on the true branch
 * @property sndOption list of nodes representing the option on the false branch
 */
@Suppress("CLASS_NAME_INCORRECT")
class IGraphCondNode(
    override val body: Node,
    override val packageName: String,
    val cond: MutableList<Node>,
    val fstOption: MutableList<Node>,
    val sndOption: MutableList<Node>
) : IGraphNode(body, packageName)
