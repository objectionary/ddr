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

package org.objectionary.ddr.unit.graph.inner

import org.objectionary.ddr.graph.AttributesSetter
import org.objectionary.ddr.graph.GraphBuilder
import org.objectionary.ddr.graph.InnerPropagator
import org.objectionary.ddr.graph.repr.IGraphNode
import org.objectionary.ddr.sources.SrsTransformed
import org.objectionary.ddr.transform.XslTransformer
import org.objectionary.ddr.unit.UnitTestBase
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Base class for inner attributes propagation testing
 */
open class InnerBase : UnitTestBase {
    override val logger = LoggerFactory.getLogger(this.javaClass.name)
    override val postfix = "tmp"

    override fun doTest() {
        val testName = getTestName()
        val sources = SrsTransformed(constructInPath(testName!!), XslTransformer(), postfix)
        val graph = GraphBuilder(sources.walk()).createGraph()
        AttributesSetter(graph).setAttributes()
        InnerPropagator(graph).propagateInnerAttrs()
        val actual = stringOutput(graph.igNodes)
        val expected = File(constructOutPath(testName)).bufferedReader().use { it.readText() }
        logger.debug(actual)
        checkOutput(expected, actual)
        deleteTempDir(sources.inPath)
    }

    override fun constructOutPath(directoryName: String): String =
        "src${sep}test${sep}resources${sep}unit${sep}out${sep}inner$sep$directoryName.txt"

    private fun stringOutput(
        nodes: Set<IGraphNode>
    ): String {
        val out = ByteArrayOutputStream()
        nodes.sortedBy { it.name }.forEach { node ->
            out.write("NODE: ${node.name} ATTRIBUTES:\n".toByteArray())
            node.attributes.forEach { out.write("name=${it.name}, dist=${it.parentDistance}\n".toByteArray()) }
        }
        return String(out.toByteArray())
    }
}
