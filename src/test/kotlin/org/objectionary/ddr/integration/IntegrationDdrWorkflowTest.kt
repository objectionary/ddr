package org.objectionary.ddr.integration

import org.junit.jupiter.api.Test
import kotlin.test.Ignore

class IntegrationDdrWorkflowTest : IntegrationDdrLaunchedBase() {
    @Test
    @Ignore
    fun `test fibonacci`() = doTest()

    @Test
    fun `test basic cycle`() = doTest()
}
