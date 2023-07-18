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

package org.objectionary.ddr

import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import java.io.File
import java.io.IOException
import java.nio.file.Path
import kotlin.test.assertEquals

/**
 * Common interface for all test classes
 */
interface TestBase {
    /** @property postfix postfix for temporary output directories */
    val postfix: String

    /** @property logger logger */
    val logger: Logger

    /**
     * File path separator
     */
    @Suppress("CUSTOM_GETTERS_SETTERS")
    val sep: Char
        get() = File.separatorChar

    /**
     * Constructs test execution process
     */
    fun doTest()

    /**
     * Compares expected test output with the actual one
     *
     * @param expected expected output
     * @param actual actual output
     */
    @Suppress("KDOC_WITHOUT_RETURN_TAG")
    fun checkOutput(
        expected: String,
        actual: String
    ) =
        assertEquals(
            expected.replace("\n", "").replace("\r", ""),
            actual.replace("\n", "").replace("\r", "")
        )

    /**
     * @param directoryName name of the input directory
     * @return path to input location
     */
    fun constructInPath(directoryName: String): String

    /**
     * @param directoryName name of the output directory
     * @return path to output location
     */
    fun constructOutPath(directoryName: String): String

    /**
     * @return name of the test being executed
     */
    fun getTestName(): String {
        Thread.currentThread().stackTrace.forEach {
            val methodName = it.methodName
            if (methodName.substring(0, "test ".length) == "test ") {
                return methodName.substring("test ".length).replace(' ', '_')
            }
        }
        throw TestNotFoundException("Test name not found")
    }

    /**
     * Deletes temporary output directory
     *
     * @param pathToSource path to source directory
     * @throws IOException
     */
    fun deleteTempDir(pathToSource: Path) {
        val tmpDir = File("${pathToSource}_$postfix")
        try {
            FileUtils.deleteDirectory(tmpDir)
        } catch (e: IOException) {
            logger.error(e.message)
            throw e
        }
    }
}
