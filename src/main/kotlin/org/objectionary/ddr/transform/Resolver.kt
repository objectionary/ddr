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

package org.objectionary.ddr.transform

import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.FileOutputStream

/**
 * Abstract class for all resolvers
 */
abstract class Resolver(private val documents: MutableMap<Document, String>) {
    /**
     * Performs the resolution
     */
    abstract fun resolve()

    /**
     * Applies transformer to each document in [documents]
     */
    protected fun transformDocuments() {
        documents.forEach { doc ->
            val outputStream = FileOutputStream(doc.value)
            outputStream.use { XslTransformer().writeXml(it, doc.key) }
        }
    }

    /**
     * Removes all siblings that go after [node]
     *
     * @param node to be modified,
     * so all of its sibling need to be removed and added after modification
     * @return list of removed siblings
     */
    protected fun removeSiblings(node: Node): MutableSet<Node> {
        val parent = node.parentNode
        val siblings = mutableSetOf(node)
        var tmpNode = node
        while (tmpNode.nextSibling != null) {
            siblings.add(tmpNode.nextSibling)
            tmpNode = tmpNode.nextSibling
        }
        siblings.forEach {
            parent.removeChild(it)
        }
        return siblings
    }
}
