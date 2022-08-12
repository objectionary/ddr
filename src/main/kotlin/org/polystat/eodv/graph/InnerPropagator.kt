package org.polystat.eodv.graph

import org.w3c.dom.Document

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
        val base = tmpKey.attributes.getNamedItem("base")?.textContent
        when(base) {
            "^" -> println() // в предке уже должен быть нужный атрибут, потом идти по цепочке
            "$" -> println() // todo
            else -> println() // идти по ссылкам
        }
        println()
    }

}