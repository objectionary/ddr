package org.polystat.eodv.graph

import java.io.File
import kotlin.test.assertEquals

interface TestBase {
    
    val sep: Char
        get() = File.separatorChar

    fun doTest()

    fun checkOutput(
        expected: String,
        actual: String
    ) =
        assertEquals(
            expected.replace("\n", "").replace("\r", ""),
            actual.replace("\n", "").replace("\r", "")
        )

    fun constructInPath(path: String): String = "src${sep}test${sep}resources${sep}in${sep}$path.xml"

    fun constructOutPath(path: String): String


    fun getTestName() = Thread.currentThread().stackTrace[4].methodName.substring(5).replace(' ', '_')
}