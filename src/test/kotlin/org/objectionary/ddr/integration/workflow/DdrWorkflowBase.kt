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

package org.objectionary.ddr.integration.workflow

import org.objectionary.ddr.integration.IntegrationTestBase
import org.objectionary.ddr.launch.DdrWorkflow
import org.objectionary.ddr.sources.SrsTransformed
import org.objectionary.ddr.transform.XslTransformer
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Base class for testing decorators resolver
 */
open class DdrWorkflowBase : IntegrationTestBase {
    override val logger = LoggerFactory.getLogger(this.javaClass.name)
    override val postfix = "tmp"

    override fun doTest() {
        val testName = getTestName()
        val sources = SrsTransformed(constructInPath(testName), XslTransformer(), postfix)
        val documents = sources.walk()
        DdrWorkflow(documents).launch()
        documents.forEach { doc ->
            val expected = File(doc.value).bufferedReader().use { it.readText().replace(" ", "") }
            val actualFilename = doc.value.replace("in$sep", "out$sep")
                .replaceFirst("${testName}_$postfix", testName)
            val actual = File(actualFilename).bufferedReader().use { it.readText().replace(" ", "") }
            checkOutput(expected, actual)
        }
        deleteTempDir(sources.inputPath)
    }
}
