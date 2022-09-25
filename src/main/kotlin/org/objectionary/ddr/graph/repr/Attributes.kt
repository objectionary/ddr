package org.objectionary.ddr.graph.repr

import org.w3c.dom.Node

/**
 * Graph attribute representation
 *
 * @property name name of the attribute
 * @property parentDistance distance to the parent, from which this attribute was pushed to current node
 * @property body corresponding xml file node
 */
@Suppress("CLASS_NAME_INCORRECT")
open class IGraphAttr(
    open val name: String,
    open val parentDistance: Int,
    open val body: Node
) {
  val freeVars: MutableSet<String> = mutableSetOf()
}

/**
 * Conditional graph attribute representation
 *
 * @property name name of the attribute
 * @property parentDistance distance to the parent, from which this attribute was pushed to current node
 * @property body corresponding xml file node
 * @property cond list of nodes representing the condition
 * @property fstOption list of nodes representing the option on the true branch
 * @property sndOption list of nodes representing the option on the false branch
 */
@Suppress("CLASS_NAME_INCORRECT")
class IGraphCondAttr(
    name: String,
    parentDistance: Int,
    body: Node,
    val cond: MutableList<Node>,
    val fstOption: MutableList<Node>,
    val sndOption: MutableList<Node>
) : IGraphAttr(
    name,
    parentDistance,
    body
)
