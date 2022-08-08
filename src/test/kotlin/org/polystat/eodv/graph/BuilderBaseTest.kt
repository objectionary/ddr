package org.polystat.eodv.graph

import org.polystat.eodv.launch.buildGraph
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.test.assertEquals

open class BuilderBaseTest {

    fun doTest() {
        val path = Thread.currentThread().stackTrace[2].methodName.substring(5).replace(' ', '_')
        val graph = buildGraph(path, constructInPath(path))
        val out = ByteArrayOutputStream()
        graph.heads.forEach { printOut(it, out, mutableSetOf()) }
        val actual = String(out.toByteArray())
        val bufferedReader: BufferedReader = File(constructOutPath(path)).bufferedReader()
        val expected = bufferedReader.use { it.readText() }
        println(actual) // debug
        checkOutput(expected, actual)
    }

    private fun constructInPath(path: String): String = "src/test/resources/graph/in/$path.xml"

    private fun constructOutPath(path: String): String = "src/test/resources/graph/out/$path.txt"

    private fun checkOutput(
        expected: String,
        actual: String
    ) =
        assertEquals(
            expected.replace("\n", "").replace("\r", ""),
            actual.replace("\n", "").replace("\r", "")
        )

    private fun printOut(
        node: IGraphNode,
        out: ByteArrayOutputStream,
        nodes: MutableSet<IGraphNode>
    ) {
        out.write("NODE: ${node.body.attributes.getNamedItem("name")}\n".toByteArray())
        if (!nodes.contains(node)) {
            nodes.add(node)
        } else {
            return
        }
        node.children.forEach {
            out.write("${node.body.attributes.getNamedItem("name").textContent} CHILD:\n".toByteArray())
            printOut(it, out, nodes)
        }
    }
}