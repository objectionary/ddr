package org.objectionary.ddr.integration

import org.objectionary.ddr.TestBase
import java.io.File

interface IntegrationTestBase: TestBase {
    override fun constructInPath(directoryName: String): String =
        "src${sep}test${sep}resources${sep}integration${sep}in$sep$directoryName"

    override fun constructOutPath(directoryName: String): String =
        "src${sep}test${sep}resources${sep}integration${sep}out$sep$directoryName"
}