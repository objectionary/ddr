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

fun abstract(node: Node?) = node?.attributes?.getNamedItem("abstract")

fun name(node: Node?) = node?.attributes?.getNamedItem("name")?.textContent

fun base(node: Node?) = node?.attributes?.getNamedItem("base")?.textContent

fun ref(node: Node?) = node?.attributes?.getNamedItem("ref")?.textContent

fun line(node: Node?) = node?.attributes?.getNamedItem("line")?.textContent

fun pos(node: Node?) = node?.attributes?.getNamedItem("pos")?.textContent

fun findRef(node: Node, objects: MutableList<Node>): Node? {
    val ref = ref(node) ?: return null
    objects.forEach {
        if (line(it) == ref) {
            return if (abstract(it) != null) it
            else findRef(it, objects)
        }
    }
    return null
}