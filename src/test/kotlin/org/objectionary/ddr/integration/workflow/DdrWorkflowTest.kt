package org.objectionary.ddr.integration.workflow

import org.junit.jupiter.api.Test
import kotlin.test.Ignore

class DdrWorkflowTest : DdrWorkflowBase() {
    @Test
    @Ignore
    fun `test fibonacci`() = doTest()

    @Test
    fun `test basic cycle`() = doTest()

    /**
     * @todo #115:90m/DEV Now this test doesn't work. Fix xsl transformations to resolve this test.
     */
    @Test
    @Ignore
    fun `test basic cycle vertical`() = doTest()
}
