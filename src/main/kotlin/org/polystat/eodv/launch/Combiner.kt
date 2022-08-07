package org.polystat.eodv.launch

import org.polystat.eodv.graph.Graph
import org.polystat.eodv.graph.GraphBuilder
import org.polystat.eodv.transform.XslTransformer
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

private const val ADD_REFS_XSL = "src\\main\\resources\\add-refs.xsl" // todo separator
private const val OUTPUT_DIR = "src\\main\\resources\\tmp_out"

fun launch(filename: String, path: String): Graph {
    val transformer = XslTransformer()
    val out = "$OUTPUT_DIR\\$filename.xml"
    Files.createDirectories(Path(OUTPUT_DIR))
    transformer.createXsl(path, out, ADD_REFS_XSL)
    val builder = GraphBuilder()
    builder.createGraph(out)
    return builder.graph
}
