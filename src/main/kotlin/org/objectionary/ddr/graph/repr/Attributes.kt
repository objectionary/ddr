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
)

class IGraphCondAttr(
    override val name: String,
    override val parentDistance: Int,
    override val body: Node
) : IGraphAttr(name, parentDistance, body)