package org.polystat.eodv.graph

import org.w3c.dom.Node
import org.w3c.dom.NodeList

fun abstract(node: Node?) = node?.attributes?.getNamedItem("abstract")

fun name(node: Node?) = node?.attributes?.getNamedItem("name")?.textContent

fun base(node: Node?) = node?.attributes?.getNamedItem("base")?.textContent

fun ref(node: Node?) = node?.attributes?.getNamedItem("ref")?.textContent

fun line(node: Node?) = node?.attributes?.getNamedItem("line")?.textContent

fun pos(node: Node?) = node?.attributes?.getNamedItem("pos")?.textContent

fun findRef(node: Node, objects: NodeList): Node? {
    val ref = ref(node) ?: return null
    for (i in 0..objects.length) {
        val item = objects.item(i) ?: continue
        if (line(item) == ref) {
            return if (abstract(item) != null) item
            else findRef(item, objects)
        }
    }
    return null
}