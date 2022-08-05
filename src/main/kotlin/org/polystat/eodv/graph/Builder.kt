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
                val toBeRemoved: MutableSet<IGraphNode> = mutableSetOf()
                graph.heads.forEach { thinOutHeads(it, toBeRemoved, mutableSetOf()) }
                toBeRemoved.forEach { graph.heads.remove(it) }
            }
        } catch (e: Exception) {
            when (e) {
                is ParserConfigurationException, is SAXException, is IOException, is TransformerException ->
                    logger.error { e.message }
                else -> throw e
            }
        }
    }

    private fun abstracts(objects: NodeList): MutableMap<String, Node> {
        val abstracts: MutableMap<String, Node> = mutableMapOf()
        for (i in 0..objects.length) {
            val node = objects.item(i)
            val name = node?.attributes?.getNamedItem("name")?.textContent
            if (node?.attributes?.getNamedItem("abstract") != null && name != null) {
                abstracts[name] = node
            }
        }
        return abstracts
    }

    private fun constructInheritance(document: Document) {
        val objects = document.getElementsByTagName("o")
        val abstracts = abstracts(objects)
        for (i in 0..objects.length) {
            val node = objects.item(i)
            val name = node?.attributes?.getNamedItem("name")?.textContent
            if (name != null && name == "@") {
                // check that @ attribute's base has an abstract object in this program
                val base = node.attributes.getNamedItem("base")?.textContent
                if (base != null && abstracts.contains(base)) {
                    val parentName = node.parentNode?.attributes?.getNamedItem("name")?.textContent
                    if (parentName != null && abstracts.containsKey(parentName)) {
                        val igChild =
                            graph.igNodes.getOrPut(parentName) { IGraphNode(parentName, node.parentNode) }
                        val igParent = graph.igNodes.getOrPut(base) { IGraphNode(base, abstracts[base]!!) }
                        graph.connect(igChild, igParent)
                    }
                }
            }
        }
    }

    private fun setLeaves() =
        graph.igNodes.filter { it.value.children.isEmpty() }.forEach { graph.leaves.add(it.value) }

    private fun setHeads(node: IGraphNode, visited: MutableMap<IGraphNode, Boolean>) {
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
        visited: MutableSet<IGraphNode>
    ) {
        if (toBeRemoved.contains(node)) return
        if (!toBeRemoved.contains(node) && visited.contains(node)) {
            toBeRemoved.add(node)
            return
        }
        visited.add(node)
        node.children.forEach { thinOutHeads(it, toBeRemoved, visited) }
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