package org.polystat.eodv.launch

import mu.KotlinLogging
import org.polystat.eodv.graph.AttributesSetter
import org.polystat.eodv.graph.Graph
import org.polystat.eodv.graph.GraphBuilder
import org.polystat.eodv.graph.InnerPropagator
import org.polystat.eodv.transform.BasicDecoratorsResolver
import org.polystat.eodv.transform.XslTransformer
import org.w3c.dom.Document
import org.xml.sax.SAXException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import kotlin.io.path.Path

private val logger = KotlinLogging.logger {}
private val sep = File.separatorChar
private val ADD_REFS_XSL = "src${sep}main${sep}resources${sep}add-refs.xsl"
private val OUTPUT_DIR = "src${sep}main${sep}resources${sep}tmp_out"
var document: Document? = null

fun launch(
    filename: String,
    path: String,
    outPath: String
) {
    val graph = buildGraph(filename, path)
    processAttributes(graph)
    val innerPropagator = InnerPropagator(document!!, graph)
    innerPropagator.propagateInnerAttrs()
    BasicDecoratorsResolver(graph, document!!, FileOutputStream(outPath)).resolveDecorators()
}

fun buildGraph(
    filename: String,
    path: String
): Graph {
    val out = "$OUTPUT_DIR${sep}$filename.xml"
    Files.createDirectories(Path(OUTPUT_DIR))
    val transformer = XslTransformer()
    transformer.createXsl(path, out, ADD_REFS_XSL)
    document = getDocument(out)
    val builder = GraphBuilder(document!!)
    builder.createGraph()
    return builder.graph
}

fun processAttributes(graph: Graph) {
    val attributesSetter = AttributesSetter(graph)
    attributesSetter.setDefaultAttributes()
    attributesSetter.pushAttributes()
}

fun getDocument(filename: String): Document? {
    try {
        val factory = DocumentBuilderFactory.newInstance()
        FileInputStream(filename).use { `is` ->
            return factory.newDocumentBuilder().parse(`is`)
        }
    } catch (e: Exception) {
        when (e) {
            is ParserConfigurationException, is SAXException ->
                logger.error { e.message }
            else -> throw e
        }
    }
    return null
}
