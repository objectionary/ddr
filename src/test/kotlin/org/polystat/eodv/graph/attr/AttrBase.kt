package org.polystat.eodv.graph.attr

import org.polystat.eodv.graph.IGraphNode
import org.polystat.eodv.graph.TestBase
import org.polystat.eodv.launch.buildGraph
import org.polystat.eodv.launch.processAttributes
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.test.assertEquals

open class AttrBase : TestBase {

    override fun doTest() {
        val path = getTestName()
        val graph = buildGraph(path, constructInPath(path))
        processAttributes(graph)
        val out = ByteArrayOutputStream()
        printOut(out, graph.igNodes)
        val actual = String(out.toByteArray())
        val bufferedReader: BufferedReader = File(constructOutPath(path)).bufferedReader()
        val expected = bufferedReader.use { it.readText() }
        println(actual) // debug
        checkOutput(expected, actual)
    }

    override fun constructOutPath(path: String): String = "src${sep}test${sep}resources${sep}out${sep}attr${sep}$path.txt"

    private fun printOut(
        out: ByteArrayOutputStream,
        nodes: Set<IGraphNode>
    ) {
        nodes.forEach { node ->
            out.write("NODE: ${node.name} ATTRIBUTES:\n".toByteArray())
            node.attributes.forEach {out.write("name=${it.name}, dist=${it.parentDistance}\n".toByteArray())}
        }
    }
}