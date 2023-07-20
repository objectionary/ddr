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

package org.objectionary.ddr.unit.graph.builder

import org.objectionary.ddr.graph.GraphBuilder
import org.objectionary.ddr.graph.repr.IGraphNode
import org.objectionary.ddr.sources.SrsTransformed
import org.objectionary.ddr.transform.XslTransformer
import org.objectionary.ddr.unit.UnitTestBase
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Base class for graph builder testing
 */
open class BuilderBase : UnitTestBase {
    override val logger = LoggerFactory.getLogger(this.javaClass.name)
    override val postfix = "tmp"

    override fun doTest() {
        val testName = getTestName()
        val sources = SrsTransformed(constructInPath(testName), XslTransformer(), postfix)
        val graph = GraphBuilder(sources.walk()).createGraph()
        val actual = stringOutput(graph.heads)
        val expected = File(constructOutPath(testName)).bufferedReader().use { it.readText() }
        logger.debug(actual)
        checkOutput(expected, actual)
        deleteTempDir(sources.inputPath)
    }

    override fun constructOutPath(directoryName: String): String =
        "src${sep}test${sep}resources${sep}unit${sep}out${sep}builder$sep$directoryName.txt"

    private fun stringOutput(
        heads: MutableSet<IGraphNode>
    ): String {
        val out = ByteArrayOutputStream()
        heads.sortedBy { it.name }.forEach { printOutRecursive(it, out, mutableSetOf()) }
        return String(out.toByteArray())
    }

    private fun printOutRecursive(
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
        node.children.sortedBy { it.name }.forEach {
            out.write("${node.name} CHILD:\n".toByteArray())
            printOutRecursive(it, out, nodes)
        }
    }
}
