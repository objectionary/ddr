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
            "^" -> decorators[key] = processParent(tmpKey)
            "$" -> {} // todo
            else -> { // tom
                val abstract = resolveRefs(tmpKey) ?: return // => tom == mouse
                resolveAttrs(tmpKey, abstract, key)
            }
        }
    }

    private fun processParent(node: Node): Boolean {
        val obj = node.parentNode // pii
        var igObj = graph.igNodes.find { it.body == obj }
        if (igObj == null) {
            graph.igNodes.add(IGraphNode(obj))
            igObj = graph.igNodes.find { it.body == obj }
        }
        val parent = node.parentNode.parentNode // mouse
        var igParent = graph.igNodes.find { it.body == parent }
        if (igParent == null) {
            graph.igNodes.add(IGraphNode(parent))
            igParent = graph.igNodes.find { it.body == parent }
        }
        val prev = igParent!!.attributes.find {
            base(node.nextSibling.nextSibling)?.substring(1) == it.name
        } // found prev chain attribute in igParent
        val prevNode = graph.igNodes.find { it.body == prev?.body } // found actual graph node of this attribute
        prevNode?.attributes?.forEach { igObj!!.attributes.add(IGraphAttr(it.name, it.parentDistance + 1, it.body)) }
        graph.connect(igObj!!, prevNode!!) // assuming length to here == 1
        return true
    }

    private fun resolveRefs(node: Node): Node? {
        // node == tom
        return if (abstract(node) != null) {
            node
        } else {
            val objects = document.getElementsByTagName("o")
            findAbstractRec(node, objects)
        }
    }

    private fun findAbstractRec(node: Node, objects: NodeList): Node? {
        val ref = ref(node) ?: return null
        for (i in 0..objects.length) {
            val item = objects.item(i) ?: continue
            if (line(item) == ref) {
                return if (abstract(item) != null) item
                else findAbstractRec(item, objects)
            }
        }
        return null
    }

    private fun resolveAttrs(node: Node, abstract: Node, key: IGraphNode) { // tom, mouse
        var tmpAbstract = graph.igNodes.find { it.body == abstract } ?: return
        var tmpNode: Node? = node.nextSibling.nextSibling ?: return // graph.igNodes.find { it.body == node }?: return
        if (base(node) == "tom") {
            println()
        }
        while (name(tmpAbstract.body) != base(key.body)?.substring(1)) {
            tmpAbstract = graph.igNodes.find { e ->
                tmpAbstract.attributes.find { base(tmpNode)?.substring(1) == name(it.body) }?.body == e.body
            } ?: return
            tmpNode = tmpNode?.nextSibling?.nextSibling
        }

        val parent = node.parentNode ?: return
        var igParent = graph.igNodes.find { it.body == parent }
        if (igParent == null) {
            graph.igNodes.add(IGraphNode(parent))
            igParent = graph.igNodes.find { it.body == parent }
        }
        tmpAbstract.attributes.forEach {
            igParent!!.attributes.add(IGraphAttr(it.name, it.parentDistance + 1, it.body))
        }
        graph.connect(igParent!!, tmpAbstract)
    }

}