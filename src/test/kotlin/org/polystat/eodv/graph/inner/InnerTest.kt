package org.polystat.eodv.graph.inner

import org.junit.jupiter.api.Test
import kotlin.test.Ignore

class InnerTest : InnerBase() {
    @Test
    fun `test multiple trees`() = doTest()

    @Test
    fun `test multiple cycles`() = doTest()

    @Test
    fun `test multiple closed cycles`() = doTest()

    @Test
    fun `test inner`() = doTest()

    @Test
    fun `test inner concrete`() = doTest()

    @Test
    fun `test inner prop`() = doTest()

    @Test
    fun `test inner ordered`() = doTest()

    @Test
//    @Ignore
    fun `test condition`() = doTest()
}