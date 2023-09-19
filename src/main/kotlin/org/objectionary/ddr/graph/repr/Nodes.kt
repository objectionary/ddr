package org.objectionary.ddr.graph.repr

import org.objectionary.ddr.util.getAttrContent
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
    val name: String? by lazy { body.getAttrContent("name") }

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

    /**
     * List of attributes of this node (inner objects and propagated attributes)
     */
    val freeVars: MutableSet<String> = mutableSetOf()
}

/**
 * Conditional graph node representation
 *
 * @property body represents the corresponding xml file node
 * @property packageName name of the package in which the described EO object is located
 * @property cond list of nodes representing the condition
 * @property fstOption list of nodes representing the option on the true branch
 * @property sndOption list of nodes representing the option on the false branch
 *
 * @todo #64:30min gather cond, fstOption and sndOption into the existing
 *  IgNodeCondition structure and refactor its usages
 */
@Suppress("CLASS_NAME_INCORRECT")
class IGraphCondNode(
    override val body: Node,
    override val packageName: String,
    val cond: IgNodeCondition,
    val fstOption: MutableList<Node>,
    val sndOption: MutableList<Node>
) : IGraphNode(body, packageName)

/**
 * Conditional node's condition representation
 *
 * @property cond list of nodes representing the condition
 */
@Suppress("CLASS_NAME_INCORRECT")
data class IgNodeCondition(
    val cond: MutableList<Node>
) {
    /**
     * Free variables of the object that is the direct parent of the condition
     */
    val freeVars: MutableSet<String> = linkedSetOf()
}
