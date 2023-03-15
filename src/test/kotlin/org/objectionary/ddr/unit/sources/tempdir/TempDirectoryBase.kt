package org.objectionary.ddr.unit.sources.tempdir

import org.objectionary.ddr.TestBase
import org.objectionary.ddr.sources.SourcesDdr
import org.objectionary.ddr.transform.XslTransformer
import java.io.File
import java.nio.file.Path
import kotlin.test.assertTrue

/**
 * Tests generation of temporary directory for modified xmir files
 */
open class TempDirectoryBase : TestBase {
    override fun doTest() {
        for (i in 0..2) {
            val strPath = constructInPath(getTestName()) + sep.toString().repeat(i)
            deleteTempDirIfExists(Path.of(strPath))
            val postfix = "ddr"
            SourcesDdr(strPath, XslTransformer(), postfix, false).walkSources()
            checkIfTempDirExists(Path.of(strPath))
            deleteTempDirIfExists(Path.of(strPath))
        }
    }

    private fun deleteTempDirIfExists(pathToSource: Path, dirPostfix: String = "ddr") {
        val strPathToTemp = "${pathToSource}_$dirPostfix"
        val tempDir = File(strPathToTemp)
        if (tempDir.exists() && tempDir.isDirectory) {
            tempDir.deleteRecursively()
        }
    }

    private fun checkIfTempDirExists(pathToSource: Path, dirPostfix: String = "ddr") {
        val strPathToTemp = "${pathToSource}_$dirPostfix"
        val tempDir = File(strPathToTemp)
        assertTrue { tempDir.exists() }
    }
    override fun constructOutPath(directoryName: String) = ""
}
