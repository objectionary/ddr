package org.polystat.eodv.graph

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.util.Objects

/**
 * Propagates inner attributes
 */
class InnerPropagator(
    private val document: Document,
    private val graph: Graph
) {
    private val decorators: MutableMap<IGraphNode, Boolean> = mutableMapOf()
    private val abstracts: MutableMap<String, MutableSet<IGraphNode>> = mutableMapOf()
//    private val abstracts: MutableSet<IGraphNode> = mutableSetOf()

    fun propagateInnerAttrs() {
        collectDecorators()
//        collectAbstracts()
        processDecorators()
    }

    private fun collectDecorators() {
        val objects = document.getElementsByTagName("o")
        for (i in 0..objects.length) {
            val node = objects.item(i)
            val name = node?.attributes?.getNamedItem("name")?.textContent
            if (name != null && name == "@") {
                decorators[IGraphNode(node)] = false
            }
            if (node?.attributes?.getNamedItem("abstract") != null && name != null) {
                abstracts.getOrPut(name) { mutableSetOf() }.add(IGraphNode(node))
            }
        }
    }

//    private fun collectAbstracts() {
//        graph.igNodes.forEach {
//            if (it.body.attributes?.getNamedItem("abstract") != null)
//                abstracts.add(it)
//        }
//    }

    private fun processDecorators() {
        while (decorators.containsValue(false)) {
            decorators.filter { !it.value }.forEach {
                getBaseAbstract(it.key)
            }
        }
    }

    private fun getBaseAbstract(key: IGraphNode) {
        var tmpKey = key.body
        while (tmpKey.attributes.getNamedItem("base")?.textContent?.startsWith('.') == true) {
            tmpKey = tmpKey.previousSibling.previousSibling
        }
        if(tmpKey.attributes.getNamedItem("base").textContent == "tom") {
            println()
        }
        when (tmpKey.attributes.getNamedItem("base")?.textContent) {
            "^" -> decorators[key] = processParent(tmpKey)
            "$" -> {} // todo
            else -> { // tom
                val abstract = resolveRefs(tmpKey)?: return // => tom == mouse
                resolveAttrs(tmpKey, abstract)
                println()
            }
        }
        println()
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
            node.nextSibling.nextSibling.attributes.getNamedItem("base").textContent.substring(1) == it.name
        } // found prev chain attribute in igParent
        val prevNode = graph.igNodes.find { it.body == prev?.body } // found actual graph node of this attribute
        prevNode?.attributes?.forEach { igObj!!.attributes.add(IGraphAttr(it.name, it.parentDistance + 1, it.body)) }
        graph.connect(igObj!!, prevNode!!)
        return true
    }

    private fun resolveRefs(node: Node): Node? {
        // node == tom
        return if (node.attributes?.getNamedItem("abstract") != null) {
            node
        } else {
            val objects = document.getElementsByTagName("o")
            findAbstractRec(node, objects)
        }
    }

    private fun findAbstractRec(node: Node, objects: NodeList): Node? {
        val ref = node.attributes?.getNamedItem("ref")?.textContent ?: return null
        for (i in 0..objects.length) {
            val item = objects.item(i)?: continue
            if (item.attributes.getNamedItem("line")?.textContent == ref) {
                if (item.attributes.getNamedItem("abstract") != null) return item
                else return findAbstractRec(item, objects)
            }
        }
        return null
    }

    private fun resolveAttrs(node: Node, abstract: Node) {

    }

}