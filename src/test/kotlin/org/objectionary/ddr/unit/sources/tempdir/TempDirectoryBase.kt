package org.objectionary.ddr.unit.sources.tempdir

import org.objectionary.ddr.sources.SrsTransformed
import org.objectionary.ddr.transform.XslTransformer
import org.objectionary.ddr.unit.UnitTestBase
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import kotlin.test.assertTrue

/**
 * Tests generation of temporary directory for modified xmir files
 */
open class TempDirectoryBase : UnitTestBase {
    override val logger = LoggerFactory.getLogger(this.javaClass.name)
    override val postfix = "tmp"
    override fun doTest() {
        for (i in 0..2) {
            val path = constructInPath(getTestName()!!) + sep.toString().repeat(i)
            SrsTransformed(path, XslTransformer(), postfix).walk()
            checkIfTempDirExists(Path.of(path))
            deleteTempDir(Path.of(path))
        }
    }

    private fun checkIfTempDirExists(pathToSource: Path) {
        val strPathToTemp = "${pathToSource}_$postfix"
        val tempDir = File(strPathToTemp)
        assertTrue { tempDir.exists() }
    }
    override fun constructOutPath(directoryName: String) = ""
}
