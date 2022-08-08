package org.polystat.eodv.graph

import mu.KotlinLogging
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList

import org.xml.sax.SAXException
import java.io.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

class GraphBuilder {
    private val logger = KotlinLogging.logger {}
    val graph = Graph()

    fun createGraph(filename: String) {
        try {
            val factory = DocumentBuilderFactory.newInstance()
            FileInputStream(filename).use { `is` ->
                val document = factory.newDocumentBuilder().parse(`is`)
                constructInheritance(document)
                setLeaves()
                graph.leaves.forEach { setHeads(it, mutableMapOf()) }
                val thinnedOutHeads: MutableSet<IGraphNode> = mutableSetOf()
                graph.heads.forEach {
                    val found = mutableListOf(false)
                    thinOutHeads(it, thinnedOutHeads, mutableSetOf(), found)
                    if (!found[0]) {
                        thinnedOutHeads.add(it)
                    }
                }
                graph.heads.clear()
                thinnedOutHeads.forEach { graph.heads.add(it) }
                processClosedCycles(graph)
            }
        } catch (e: Exception) {
            when (e) {
                is ParserConfigurationException, is SAXException, is IOException, is TransformerException ->
                    logger.error { e.message }
                else -> throw e
            }
        }
    }

    private fun abstracts(objects: NodeList): MutableMap<String, MutableSet<Node>> {
        val abstracts: MutableMap<String, MutableSet<Node>> = mutableMapOf()
        for (i in 0..objects.length) {
            val node = objects.item(i)
            val name = node?.attributes?.getNamedItem("name")?.textContent
            if (node?.attributes?.getNamedItem("abstract") != null && name != null) {
                abstracts.getOrPut(name) { mutableSetOf() }.add(node)
            }
        }
        return abstracts
    }

    private fun getAbstract(
        abstracts: MutableMap<String, MutableSet<Node>>,
        baseName: String?,
        baseRef: String?
    ): Node? {
        if (baseName != null && abstracts.contains(baseName)) {
            return abstracts[baseName]!!.find {
                it.attributes?.getNamedItem("line")?.textContent == baseRef // нет конечно
            }
        }
        return null
    }

    private fun constructInheritance(document: Document) {
        val objects = document.getElementsByTagName("o")
        val abstracts = abstracts(objects)
        for (i in 0..objects.length) {
            val node = objects.item(i)
            val name = node?.attributes?.getNamedItem("name")?.textContent
            if (name != null && name == "@") {
                // check that @ attribute's base has an abstract object in this program
                val baseNodeName = node.attributes.getNamedItem("base")?.textContent
                val baseNodeRef = node.attributes.getNamedItem("ref")?.textContent
                val abstractBaseNode = getAbstract(abstracts, baseNodeName, baseNodeRef)
                if (abstractBaseNode != null) {
                    val parentNode = node.parentNode
                    if (parentNode != null) {
                        var igChild = IGraphNode(node.parentNode)
                        val checkedChild = checkNodes(igChild)
                        if (checkedChild == null) {
                            graph.igNodes.add(igChild)
                        } else {
                            igChild = checkedChild
                        }
                        var igParent = IGraphNode(abstractBaseNode)
                        val checkedParent = checkNodes(igParent)
                        if (checkedParent == null) {
                            graph.igNodes.add(igParent)
                        } else {
                            igParent = checkedParent
                        }
                        graph.connect(igChild, igParent)
                    }
                }
            }
        }
    }

    private fun checkNodes(node: IGraphNode): IGraphNode? {
        return graph.igNodes.find { it.body.attributes == node.body.attributes }
    }

    private fun setLeaves() =
        graph.igNodes.filter { it.children.isEmpty() }.forEach { graph.leaves.add(it) }

    private fun setHeads(
        node: IGraphNode,
        visited: MutableMap<IGraphNode, Boolean>
    ) {
        if (visited.containsKey(node) || node.parents.isEmpty()) {
            graph.heads.add(node)
        } else {
            visited[node] = true
            node.parents.forEach { setHeads(it, visited) }
        }
    }

    private fun thinOutHeads(
        node: IGraphNode,
        toBeRemoved: MutableSet<IGraphNode>,
        visited: MutableSet<IGraphNode>,
        found: MutableList<Boolean>
    ) {
        if (found[0] || toBeRemoved.contains(node)) {
            found[0] = true
            return
        }
        if (visited.contains(node)) {
            toBeRemoved.add(node)
            found[0] = true
            return
        }
        visited.add(node)
        node.children.forEach { thinOutHeads(it, toBeRemoved, visited, found) }
    }

    private fun decorationCycles() {

    }

    @Throws(TransformerException::class, UnsupportedEncodingException::class)
    private fun writeXml(
        doc: Document,
        output: OutputStream
    ) {
        val prettyPrintXlst = "src/main/resources/pretty_print.xslt"
        val transformer = TransformerFactory.newInstance().newTransformer(StreamSource(File(prettyPrintXlst)))
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no")
        val source = DOMSource(doc)
        val result = StreamResult(output)
        transformer.transform(source, result)
    }
}

//data class Node(val name: String)