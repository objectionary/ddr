/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Olesia Subbotina
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.polystat.eodv.unit.graph.builder

import org.polystat.eodv.graph.IGraphNode
import org.polystat.eodv.unit.TestBase
import org.polystat.eodv.launch.buildGraph
import org.polystat.eodv.launch.documents
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File

open class BuilderBase : TestBase {

    override fun doTest() {
        val path = getTestName()
        documents.clear()
        val graph = buildGraph(constructInPath(path))
        val out = ByteArrayOutputStream()
        graph.heads.forEach { printOut(it, out, mutableSetOf()) }
        val actual = String(out.toByteArray())
        val bufferedReader: BufferedReader = File(constructOutPath(path)).bufferedReader()
        val expected = bufferedReader.use { it.readText() }
        println(actual) // debug
        checkOutput(expected, actual)
    }

    override fun constructOutPath(path: String): String = "src${sep}test${sep}resources${sep}unit${sep}out${sep}builder${sep}$path.txt"

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