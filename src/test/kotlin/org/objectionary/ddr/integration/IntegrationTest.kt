package org.objectionary.ddr.integration

import org.junit.jupiter.api.Test
import kotlin.test.Ignore

class IntegrationTest : IntegrationBase() {
    @Test
    @Ignore
    fun `test fibonacci`() = doTest()

    @Test
    fun `test basic cycle`() = doTest()
}
