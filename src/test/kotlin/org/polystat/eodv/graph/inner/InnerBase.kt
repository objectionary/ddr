package org.polystat.eodv.graph.inner

import org.polystat.eodv.graph.IGraphNode
import org.polystat.eodv.graph.InnerPropagator
import org.polystat.eodv.graph.TestBase
import org.polystat.eodv.launch.buildGraph
import org.polystat.eodv.launch.document
import org.polystat.eodv.launch.processAttributes
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File

open class InnerBase : TestBase {

    override fun doTest() {
        val path = getTestName()
        val graph = buildGraph(path, constructInPath(path))
        processAttributes(graph)
        val innerPropagator = InnerPropagator(document!!, graph)
        innerPropagator.propagateInnerAttrs()
        val out = ByteArrayOutputStream()
//        printOut(out, graph.igNodes)
        val actual = String(out.toByteArray())
        val bufferedReader: BufferedReader = File(constructOutPath(path)).bufferedReader()
        val expected = bufferedReader.use { it.readText() }
//        println(actual) // debug
        checkOutput(expected, actual)
    }

    override fun constructOutPath(path: String): String = "src${sep}test${sep}resources${sep}out${sep}inner${sep}$path.txt"

    private fun printOut(
        node: IGraphNode,
        out: ByteArrayOutputStream,
        nodes: MutableSet<IGraphNode>
    ) {
        out.write("NODE: name=\"${node.name}\"\n".toByteArray())
        if (!nodes.contains(node)) {
            nodes.add(node)
        } else {
            return
        }
        node.children.forEach {
            out.write("${node.name} CHILD:\n".toByteArray())
            printOut(it, out, nodes)
        }
    }
}