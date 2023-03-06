package org.objectionary.ddr.unit.files.temp_directory

import org.objectionary.ddr.TestBase
import org.objectionary.ddr.launch.buildGraph
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import kotlin.test.assertTrue

/**
 * Tests generation of temporary directory for modified xmir files
 */
open class TempDirectoryBase: TestBase {
    override fun doTest() {
        for (i in 0..2) {
            val path = constructInPath(getTestName()) + sep.toString().repeat(i)
            val dirPostfix = "ddr"
            deleteTempDirIfExists(Path.of(path))
            buildGraph(path, false, dirPostfix)
            checkIfTempDirExists(Path.of(path))
            deleteTempDirIfExists(Path.of(path))
        }
    }

    private fun deleteTempDirIfExists(pathToSource: Path, dirPostfix: String = "ddr") {
        val strPathToTemp = pathToSource.toString() + "_" + dirPostfix
        val tempDir = File(strPathToTemp)
        if (tempDir.exists() && tempDir.isDirectory) {
            tempDir.deleteRecursively()
        }
    }

    private fun checkIfTempDirExists(pathToSource: Path, dirPostfix: String = "ddr") {
        val strPathToTemp = pathToSource.toString() + "_" + dirPostfix
        val tempDir = File(strPathToTemp)
        assertTrue { tempDir.exists() }
    }
    override fun constructOutPath(directoryName: String) = ""

}