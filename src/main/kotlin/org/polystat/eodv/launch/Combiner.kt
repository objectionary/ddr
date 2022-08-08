package org.polystat.eodv.launch

import org.polystat.eodv.graph.AttributesSetter
import org.polystat.eodv.graph.Graph
import org.polystat.eodv.graph.GraphBuilder
import org.polystat.eodv.transform.XslTransformer
import java.nio.file.Files
import kotlin.io.path.Path

private const val ADD_REFS_XSL = "src\\main\\resources\\add-refs.xsl" // todo separator
private const val OUTPUT_DIR = "src\\main\\resources\\tmp_out"

fun launch(
    filename: String,
    path: String
) {
    val graph = buildGraph(filename, path)
    processAttributes(graph)
}

fun buildGraph(
    filename: String,
    path: String
): Graph {
    val out = "$OUTPUT_DIR\\$filename.xml"
    Files.createDirectories(Path(OUTPUT_DIR))
    val transformer = XslTransformer()
    transformer.createXsl(path, out, ADD_REFS_XSL)
    val builder = GraphBuilder()
    builder.createGraph(out)
    return builder.graph
}

fun processAttributes(graph: Graph) {
    val attributesSetter = AttributesSetter(graph)
    attributesSetter.setSpecifiedAttributes()
    attributesSetter.pushAttributes()
}
