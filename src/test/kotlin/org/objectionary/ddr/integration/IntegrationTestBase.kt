package org.objectionary.ddr.integration

import org.objectionary.ddr.TestBase

/**
 * Interface for all integration test classes
 */
interface IntegrationTestBase : TestBase {
    override fun constructInPath(directoryName: String): String =
        "src${sep}test${sep}resources${sep}integration${sep}in$sep$directoryName"

    override fun constructOutPath(directoryName: String): String =
        "src${sep}test${sep}resources${sep}integration${sep}out$sep$directoryName"

    /**
     * @param directoryName name of the result directory
     * @return path to output location
     */
    fun constructResultPath(directoryName: String) =
        "${constructInPath(directoryName).substringBeforeLast(sep)}$sep${directoryName}_$postfix"
}
