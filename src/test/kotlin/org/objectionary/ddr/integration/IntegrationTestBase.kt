package org.objectionary.ddr.integration

import org.objectionary.ddr.TestBase
import java.nio.file.Path

/**
 * Interface for all integration test classes
 */
interface IntegrationTestBase : TestBase {
    override fun constructInPath(dirname: String): Path =
        Path.of("src${sep}test${sep}resources${sep}integration${sep}in$sep$dirname")

    override fun constructOutPath(dirname: String): Path =
        Path.of("src${sep}test${sep}resources${sep}integration${sep}out$sep$dirname")

    /**
     * @param dirname name of the result directory
     * @return path to output location
     */
    fun constructResultPath(dirname: String) =
        Path.of("${constructInPath(dirname).parent}$sep${dirname}_$postfix")
}
