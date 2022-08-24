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

package org.polystat.eodv.graph

import org.w3c.dom.Node

/**
 * Decoration hierarchy graph representation
 */
class Graph {
    val igNodes: MutableSet<IGraphNode> = mutableSetOf()
    val initialObjects: MutableList<Node> = mutableListOf()
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
    val body: Node,
    val packageName: String
) {
//    val name: String by lazy { name(body) }
    val name: String? = name(body) // debug
    val children: MutableSet<IGraphNode> = mutableSetOf()
    val parents: MutableSet<IGraphNode> = mutableSetOf()
    val attributes: MutableList<IGraphAttr> = mutableListOf()
}

/**
 * Graph attribute representation
 * @property name is the name of the attribute
 * @property parentDistance is the distance to the parent, from which this attribute was pushed to current node
 * @property body represents the corresponding xml file node
 */
data class IGraphAttr(
    val name: String,
    val parentDistance: Int,
    val body: Node
)