package org.objectionary.ddr.transform

import org.objectionary.ddr.graph.repr.Graph
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.FileOutputStream

/**
 *
 */
abstract class Resolver(private val documents: MutableMap<Document, String>) {
    abstract fun resolve()

    protected fun transformDocuments() {
        documents.forEach { doc ->
            val outputStream = FileOutputStream(doc.value)
            outputStream.use { XslTransformer().writeXml(it, doc.key) }
        }
    }

    protected fun gg(node: Node): MutableSet<Node> {
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