package org.polystat.eodv.graph

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList

/**
 * Propagates inner attributes
 */
class InnerPropagator(
    private val document: Document,
    private val graph: Graph
) {
    private val decorators: MutableMap<IGraphNode, Boolean> = mutableMapOf()
    private val abstracts: MutableMap<String, MutableSet<IGraphNode>> = mutableMapOf()

    fun propagateInnerAttrs() {
        collectDecorators()
        processDecorators()
    }

    private fun collectDecorators() {
        val objects = document.getElementsByTagName("o")
        for (i in 0..objects.length) {
            val node = objects.item(i)
            val name = name(node)
            if (name != null && name == "@") {
                decorators[IGraphNode(node)] = false
            }
            if (abstract(node) != null && name != null) {
                abstracts.getOrPut(name) { mutableSetOf() }.add(IGraphNode(node))
            }
        }
    }

    private fun processDecorators() {
//        while (decorators.containsValue(false)) { // todo
        for (i in 0..5)
            decorators.filter { !it.value }.forEach {
                getBaseAbstract(it.key)
            }
//        }
    }

    private fun getBaseAbstract(key: IGraphNode) {
        var tmpKey = key.body
        while (base(tmpKey)?.startsWith('.') == true) {
            tmpKey = tmpKey.previousSibling.previousSibling
        }
        when (base(tmpKey)) {
            "^" -> {
                val abstract = tmpKey.parentNode.parentNode
                resolveAttrs(tmpKey, abstract, key)
            }
            "$" -> {} // todo
            else -> {
                val abstract = resolveRefs(tmpKey) ?: return // => tom == mouse
                resolveAttrs(tmpKey, abstract, key)
            }
        }
    }

    private fun resolveRefs(node: Node): Node? {
        return if (abstract(node) != null) {
            node
        } else {
            val objects = document.getElementsByTagName("o")
            findRef(node, objects)
        }
    }

    private fun resolveAttrs(node: Node, abstract: Node, key: IGraphNode): Boolean { // tom, mouse
        var tmpAbstract = graph.igNodes.find { it.body == abstract } ?: return false
        var tmpNode: Node? = node.nextSibling.nextSibling ?: return false // graph.igNodes.find { it.body == node }?: return
        while (name(tmpAbstract.body) != base(key.body)?.substring(1)) {
            tmpAbstract = graph.igNodes.find { e ->
                tmpAbstract.attributes.find { base(tmpNode)?.substring(1) == name(it.body) }?.body == e.body
            } ?: return false
            tmpNode = tmpNode?.nextSibling?.nextSibling
        }
        val parent = node.parentNode ?: return false // rat_pii
        var igParent = graph.igNodes.find { it.body == parent }
        if (igParent == null) {
            graph.igNodes.add(IGraphNode(parent))
            igParent = graph.igNodes.find { it.body == parent }
        }
        tmpAbstract.attributes.forEach {
            if (igParent!!.attributes.find { a -> it.body == a.body } == null) {
                igParent.attributes.add(IGraphAttr(it.name, it.parentDistance + 1, it.body))
            }
        }
        graph.connect(igParent!!, tmpAbstract)
        return true
    }

}