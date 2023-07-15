package org.objectionary.ddr.unit.sources.tempdir

import org.objectionary.ddr.TestBase
import org.objectionary.ddr.sources.SrsTransformed
import org.objectionary.ddr.transform.XslTransformer
import org.objectionary.ddr.unit.UnitTestBase
import java.io.File
import java.nio.file.Path
import kotlin.test.assertTrue

/**
 * Tests generation of temporary directory for modified xmir files
 */
open class TempDirectoryBase : UnitTestBase {
    private val postfix = "tmp"
    override fun doTest() {
        for (i in 0..2) {
            val path = constructInPath(getTestName()!!) + sep.toString().repeat(i)
            deleteTempDirIfExists(Path.of(path))
            SrsTransformed(path, XslTransformer(), postfix).walk()
            checkIfTempDirExists(Path.of(path))
            deleteTempDirIfExists(Path.of(path))
        }
    }

    private fun deleteTempDirIfExists(pathToSource: Path) {
        val strPathToTemp = "${pathToSource}_$postfix"
        val tempDir = File(strPathToTemp)
        if (tempDir.exists() && tempDir.isDirectory) {
            tempDir.deleteRecursively()
        }
    }

    private fun checkIfTempDirExists(pathToSource: Path) {
        val strPathToTemp = "${pathToSource}_$postfix"
        val tempDir = File(strPathToTemp)
        assertTrue { tempDir.exists() }
    }
    override fun constructOutPath(directoryName: String) = ""
}
