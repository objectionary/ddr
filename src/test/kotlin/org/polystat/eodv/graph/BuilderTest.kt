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
    fun `test cycle`() = doTest("basic_cycle")

    @Test
    fun `test triple cycle`() = doTest("triple_cycle")

    @Test
    fun `test multiple cycles`() = {}

    @Test
    fun `test inner`() = doTest("inner")
}