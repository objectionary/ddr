package org.polystat.eodv.graph

import org.junit.jupiter.api.Test

class BuilderTest : BaseTest() {

    @Test
    fun `test simple tree`() = doTest("animals")

    @Test
    fun `test large tree`() = doTest("creatures")

    @Test
    fun `test multiple trees`() = doTest("double_hie")

    @Test
    fun `test cycle`() = doTest("one_cycle")

    @Test
    fun `test triple cycle`() = doTest("triple")

    @Test
    fun `test multiple cycles`() = {}
}