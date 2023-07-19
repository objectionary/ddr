package org.objectionary.ddr.unit

import org.objectionary.ddr.TestBase
import java.nio.file.Path

/**
 * Interface for all integration test classes
 */
interface UnitTestBase : TestBase {
    override fun constructInPath(dirname: String): Path =
        Path.of("src${sep}test${sep}resources${sep}unit${sep}in$sep$dirname")
}
