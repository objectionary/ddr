package org.polystat.eodv.transform

import org.polystat.eodv.graph.*
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList

class BasicDecoratorsResolver(private val graph: Graph, private val document: Document) {

    private val declarations: MutableMap<Node, Node?> = mutableMapOf()

    fun resolveDecorators() {
        collectDeclarations()
        resolveRefs()
    }

    private fun collectDeclarations() {
        val objects = document.getElementsByTagName("o")
        for (i in 0..objects.length) {
            val node = objects.item(i)
            val name = name(node) ?: continue
            val base = base(node) ?: continue
            if (abstract(node) == null && name != "@" && !base.startsWith('.')) {
                declarations[node] = null
            }
        }
    }

    private fun resolveRefs() {
        val objects = document.getElementsByTagName("o")
        declarations.keys.forEach { declarations[it] = findRef(it, objects) }
    }

    private fun inject() {
        val objects = document.getElementsByTagName("o")
        for (i in 0..objects.length) {
            val node = objects.item(i)
            val name = name(node)
            val base = base(node) ?: continue
            val ref = ref(node) ?: continue
            if (name == null) {
                val baseObject = firstRef(ref, objects) // jerry2
            }
        }
    }

    private fun firstRef(ref: String, objects: NodeList): Node? {
        for (i in 0..objects.length) {
            val item = objects.item(i) ?: continue
            if (line(item) == ref) {
                return item
            }
        }
        return null
    }
}