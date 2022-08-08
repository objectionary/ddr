package org.polystat.eodv.graph

import org.junit.jupiter.api.Test

class BuilderTest : BaseTest() {

    @Test
    fun `test basic tree`() = doTest("basic_tree")

    @Test
    fun `test tree`() = doTest("tree")

    @Test
    fun `test multiple trees`() = doTest("multiple_trees")

    @Test
    fun `test basic cycle`() = doTest("basic_cycle")

    @Test
    fun `test triple cycle`() = doTest("triple_cycle")

    @Test
    fun `test multiple cycles`() = doTest("multiple_cycles")

    @Test
    fun `test closed cycle`() = doTest("closed_cycle")

    @Test
    fun `test multiple closed cycles`() = doTest("multiple_closed_cycles")

    @Test
    fun `test inner`() = doTest("inner")

    @Test
    fun `test inner concrete`() = doTest("inner_concrete")
}