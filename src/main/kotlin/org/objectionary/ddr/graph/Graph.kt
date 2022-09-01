/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Olesia Subbotina
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.objectionary.ddr.graph

import org.w3c.dom.Node

/**
 * Decoration hierarchy graph representation
 */
class Graph {
    /**
     * Collection of all graph nodes
     */
    val igNodes: MutableSet<IGraphNode> = mutableSetOf()

    /**
     * Collection of initial xml objects
     */
    val initialObjects: MutableList<Node> = mutableListOf()

    /**
     * "Root nodes" of the graph, it is guaranteed that the whole graph can be traversed starting from these nodes
     */
    val heads: MutableSet<IGraphNode> = mutableSetOf()

    /**
     * Leaf nodes of the graph, they don't have any children
     */
    val leaves: MutableList<IGraphNode> = mutableListOf()

    /**
     * Connects [child] with [parent] in the graph
     *
     * @param child child node
     * @param parent parent node
     */
    fun connect(
        child: IGraphNode,
        parent: IGraphNode
    ) {
        child.parents.add(parent)
        parent.children.add(child)
    }
}

/**
 * Graph node representation. Alternative representation of EO object
 *
 * @property body represents the corresponding xml file node
 * @property packageName name of the package in which the described EO object is located
 */
@Suppress("CLASS_NAME_INCORRECT")
data class IGraphNode(
    val body: Node,
    val packageName: String
) {
    /**
     * Node name
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
 * Graph attribute representation
 *
 * @property name is the name of the attribute
 * @property parentDistance is the distance to the parent, from which this attribute was pushed to current node
 * @property body represents the corresponding xml file node
 */
@Suppress("CLASS_NAME_INCORRECT")
data class IGraphAttr(
    val name: String,
    val parentDistance: Int,
    val body: Node
)
