package org.polystat.eodv.graph.resolver

import org.polystat.eodv.graph.InnerPropagator
import org.polystat.eodv.graph.TestBase
import org.polystat.eodv.launch.buildGraph
import org.polystat.eodv.launch.document
import org.polystat.eodv.launch.processAttributes
import org.polystat.eodv.transform.BasicDecoratorsResolver
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File

open class ResolverBase : TestBase {

    override fun doTest() {
        val path = getTestName()
        val graph = buildGraph(path, constructInPath(path))
        processAttributes(graph)
        val innerPropagator = InnerPropagator(document!!, graph)
        innerPropagator.propagateInnerAttrs()
        val out = ByteArrayOutputStream()
        BasicDecoratorsResolver(graph, document!!, out).resolveDecorators()
        val actual = String(out.toByteArray())
        val bufferedReader: BufferedReader = File(constructOutPath(path)).bufferedReader()
        val expected = bufferedReader.use { it.readText() }
        println(actual)
        checkOutput(expected, actual)
    }

    override fun constructOutPath(path: String): String = "src${sep}test${sep}resources${sep}out${sep}resolver${sep}$path.xml"
}