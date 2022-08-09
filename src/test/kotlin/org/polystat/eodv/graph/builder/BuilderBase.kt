package org.polystat.eodv.graph.builder

import org.polystat.eodv.graph.IGraphNode
import org.polystat.eodv.graph.TestBase
import org.polystat.eodv.launch.buildGraph
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.test.assertEquals

open class BuilderBase : TestBase {

    override fun doTest() {
        val path = getTestName()
        val graph = buildGraph(path, constructInPath(path))
        val out = ByteArrayOutputStream()
        graph.heads.forEach { printOut(it, out, mutableSetOf()) }
        val actual = String(out.toByteArray())
        val bufferedReader: BufferedReader = File(constructOutPath(path)).bufferedReader()
        val expected = bufferedReader.use { it.readText() }
        println("ACTUAL!: $actual") // debug
        checkOutput(expected, actual)
    }

    override fun constructOutPath(path: String): String = "src${sep}test${sep}resources${sep}out${sep}builder${sep}$path.txt"

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