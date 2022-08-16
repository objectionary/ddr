package org.polystat.eodv.graph

import org.w3c.dom.Node

class AttributesSetter(private val graph: Graph) {
    fun setDefaultAttributes() {
        graph.igNodes.forEach {
            val attributes = it.body.childNodes
            for (j in 0 until attributes.length) {
                val attr: Node = attributes.item(j)
                if (abstract(attr) != null) {
                    it.attributes.add(IGraphAttr(name(attr)!!, 0, attr))
                }
            }
        }
    }

    fun pushAttributes() = graph.heads.forEach { dfsPush(it, null, mutableMapOf()) }

    private fun dfsPush(
        node: IGraphNode,
        parent: IGraphNode?,
        visited: MutableMap<IGraphNode, Int>
    ) {
        if (visited[node] == 2) return
        if (visited[node] == 1) visited[node] = 2
        else visited[node] = 1
        parent?.attributes?.filter { pa ->
            node.attributes.none { na -> na.name == pa.name }
        }?.forEach {
            node.attributes.add(IGraphAttr(it.name, it.parentDistance + 1, it.body))
        }
        node.children.forEach { dfsPush(it, node, visited) }
    }
}