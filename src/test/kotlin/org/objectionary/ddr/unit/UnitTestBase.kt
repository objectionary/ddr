package org.objectionary.ddr.unit

import org.objectionary.ddr.TestBase

/**
 * Interface for all integration test classes
 */
interface UnitTestBase : TestBase {
    override fun constructInPath(directoryName: String): String =
        "src${sep}test${sep}resources${sep}unit${sep}in$sep$directoryName"
}