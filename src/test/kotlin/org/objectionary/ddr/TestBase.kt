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
     * @param dirname name of the input directory
     * @return path to input location
     */
    fun constructInPath(dirname: String): Path

    /**
     * @param dirname name of the output directory
     * @return path to output location
     */
    fun constructOutPath(dirname: String): Path

    /**
     * @return name of the test being executed
     * @throws TestNotFoundException
     */
    fun getTestName(): String {
        Thread.currentThread().stackTrace.forEach {
            val name = it.methodName
            if (name.substring(0, "test ".length) == "test ") {
                return name.substring("test ".length).replace(' ', '_')
            }
        }
        throw TestNotFoundException("Function with a name starting with \"test\" is not found in the current stack trace")
    }

    /**
     * Deletes temporary output directory
     *
     * @param path path to source directory
     * @throws IOException
     */
    fun deleteTempDir(path: Path) {
        try {
            FileUtils.deleteDirectory(File("${path}_$postfix"))
        } catch (e: IOException) {
            throw IOException("Trying to delete not existing temporary directory", e)
        }
    }
}
