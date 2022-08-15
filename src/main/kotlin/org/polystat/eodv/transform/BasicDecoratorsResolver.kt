package org.polystat.eodv.transform

import org.polystat.eodv.graph.*
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

class BasicDecoratorsResolver(private val graph: Graph, private val document: Document) {

    private val declarations: MutableMap<Node, Node?> = mutableMapOf()

    fun resolveDecorators() {
        collectDeclarations()
        resolveRefs()
        injectAttributes()
    }

    private fun collectDeclarations() {
        val objects = document.getElementsByTagName("o")
        for (i in 0..objects.length) {
            val node = objects.item(i)
            val base = base(node) ?: continue
            if (abstract(node) == null && !base.startsWith('.')) {
                declarations[node] = null
            }
        }
    }

    private fun resolveRefs() {
        val objects = document.getElementsByTagName("o")
        declarations.keys.forEach { declarations[it] = findRef(it, objects) }
    }

    private fun injectAttributes() {
        val objects = document.getElementsByTagName("o")
        for (i in 0..objects.length) {
            val node = objects.item(i)
            val ref = ref(node) ?: continue // todo ^ doesn't have ref attr
            if (name(node) == null) {
                val baseObject = firstRef(ref, objects) // jerry2
                val abstract = getIgAbstract(baseObject) ?: continue // cat
                var sibling = node?.nextSibling?.nextSibling
                println("HERE " + base(sibling))
                while (base(sibling)?.startsWith(".") == true) {
                    val base = base(sibling)
                    val attr = abstract.attributes.find { it.name == base?.substring(1) }
                    inject(sibling, attr)
                    sibling = sibling?.nextSibling?.nextSibling
                }
            }
        }
    }

    private fun inject(node: Node?, attr: IGraphAttr?) {

    }

    private fun getIgAbstract(node: Node?): IGraphNode? {
        if (abstract(node) != null) return graph.igNodes.find { it.body == node }
        val abstract = declarations[node] ?: return null // cat
        return graph.igNodes.find { it.body == abstract }
    }

    private fun firstRef(
        ref: String,
        objects: NodeList
    ): Node? {
        for (i in 0..objects.length) {
            val item = objects.item(i) ?: continue
            if (line(item) == ref) {
                return item
            }
        }
        return null
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