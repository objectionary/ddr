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

import org.objectionary.ddr.graph.repr.Graph
import org.objectionary.ddr.graph.repr.IGraphNode
import org.w3c.dom.Node

/**
 * Finds attribute of this node by [name]
 *
 * @param [name] name of attribute to find
 * @return found attribute
 */
fun Node?.getAttr(name: String) = this?.attributes?.getNamedItem(name)?.textContent

/**
 * Finds attribute of this node by [name]
 *
 * @param [name] name of attribute to find
 * @return content of found attribute
 */
fun Node?.getAttrContent(name: String) = this?.attributes?.getNamedItem(name)?.textContent

/**
 * Finds out whether this node contains named attribute
 *
 * @param [name] name of attribute to find
 * @return `true` if this node contains an attribute named [name] and, `false` if it doesn't
 */
fun Node?.containsAttr(name: String) = this?.getAttr(name) != null

/**
 * Finds package name of the xmir file that contains this node
 *
 * @return found package name
 */
fun Node?.packageName(): String {
    val heads = this?.ownerDocument?.getElementsByTagName("head") ?: return ""
    for (i in 0 until heads.length) {
        val head = heads.item(i)
        if (head.textContent.equals("package")) {
            return head.nextSibling.nextSibling.textContent
        }
    }
    return ""
}

/**
 * Either finds an abstract object that the [node] is referring to or looks for the abstract node in the packages
 *
 * @param node node to be handled
 * @param objects list of xml objects
 * @param graph graph
 * @return node found through refs
 */
@Suppress("AVOID_NULL_CHECKS")
fun findRef(
    node: Node?,
    objects: MutableSet<Node>,
    graph: Graph
): Node? {
    val ref = node.getAttrContent("ref") ?: return getAbstractViaPackage(node.getAttrContent("base"), graph)?.body
    objects.forEach {
        if (it.getAttrContent("line") == ref) {
            if (it.containsAttr("abstract") && node.packageName() == it.packageName()) {
                return it
            }
            if (!it.containsAttr("abstract") && node.packageName() == it.packageName()) {
                val traversed = walkDotChain(it)
                return if (traversed == null) {
                    findRef(it, objects, graph)
                } else {
                    findRef(traversed, objects, graph)
                }
            }
        }
    }
    return null
}

private fun walkDotChain(
    node: Node
): Node? {
    var sibling = node.nextSibling?.nextSibling
    while (sibling.getAttrContent("base")?.startsWith(".") == true) {
        sibling = sibling?.nextSibling
        sibling?.attributes ?: run { sibling = sibling?.nextSibling }
    }
    sibling = sibling?.previousSibling
    sibling?.attributes ?: run { sibling = sibling?.previousSibling }
    return sibling
}

private fun getAbstractViaPackage(baseNodeName: String?, graph: Graph): IGraphNode? {
    val packageName = baseNodeName?.substringBeforeLast('.')
    val nodeName = baseNodeName?.substringAfterLast('.')
    return graph.igNodes.find { it.name.equals(nodeName) && it.packageName == packageName }
}
