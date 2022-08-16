package org.polystat.eodv.graph.builder

import org.junit.jupiter.api.Test

class BuilderTest : BuilderBase() {

    @Test
    fun `test basic tree`() = doTest()

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
}