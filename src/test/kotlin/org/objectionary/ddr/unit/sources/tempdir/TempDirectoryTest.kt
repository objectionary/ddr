package org.objectionary.ddr.unit.sources.tempdir

import org.junit.jupiter.api.Test

class TempDirectoryTest : TempDirectoryBase() {
    @Test
    fun `test basic tree`() = doTest()

    @Test
    fun `test multiple aliases`() = doTest()

    @Test
    fun `test tree`() = doTest()

    @Test
    fun `test multiple trees`() = doTest()

    @Test
    fun `test basic cycle`() = doTest()

    @Test
    fun `test triple cycle`() = doTest()

    @Test
    fun `test multiple cycles`() = doTest()

    @Test
    fun `test closed cycle`() = doTest()

    @Test
    fun `test multiple closed cycles`() = doTest()

    @Test
    fun `test inner`() = doTest()

    @Test
    fun `test inner concrete`() = doTest()

    @Test
    fun `test inner ordered`() = doTest()

    @Test
    fun `test basic dir`() = doTest()
}
