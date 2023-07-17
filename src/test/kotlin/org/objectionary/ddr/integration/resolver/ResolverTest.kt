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

package org.objectionary.ddr.integration.resolver

import org.junit.jupiter.api.Test
import kotlin.test.Ignore

@Ignore
class ResolverTest : ResolverBase() {
    @Test
    fun `test basic`() = doTest()

    @Test
    fun `test multiple aliases`() = doTest()

    @Test
    fun `test local object`() = doTest()

    @Test
    fun `test no dot notation`() = doTest()

    @Test
    fun `test two levels insert before`() = doTest()

    @Test
    fun `test alias chain`() = doTest()

    @Test
    fun `test fibonacci`() = doTest()

    @Test
    fun `test condition`() = doTest()

    @Test
    fun `test condition chain`() = doTest()

    @Test
    fun `test conditional attribute`() = doTest()
}
