package org.objectionary.ddr.sources

import org.objectionary.ddr.transform.XslTransformer
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory

private val sep = File.separatorChar

interface Sources {
    val path: Path //maybe make it private?
    val documents : MutableMap<Document, String>

    fun walkSources()
}

class SourcesDDR(
    strPath: String,
    private val _postfix: String = "ddr",
    private val _gather: Boolean = true
) : Sources {
    override val path: Path = Path.of(strPath)
    override val documents: MutableMap<Document, String> = mutableMapOf()
    private val _logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun walkSources() {
        val transformer = XslTransformer()
        Files.walk(path)
            .filter(Files::isRegularFile)
            .forEach {
                val tmpPath = createTempDirectories(it.toString())
                transformer.transformXml(it.toString(), tmpPath)
                documents[getDocument(tmpPath)!!] = tmpPath
            }
    }

    /**
     * Get Document from source xml file
     *
     * @param filename source xml filename
     * @return generated Document
     */
    private fun getDocument(filename: String): Document? {
        try {
            val factory = DocumentBuilderFactory.newInstance()
            FileInputStream(filename).use { return factory.newDocumentBuilder().parse(it) }
        } catch (e: Exception) {
            _logger.error(e.printStackTrace().toString())
            return null
        }
    }

    /**
     * Creates a new temporary directory for transformed xmir files
     *
     * @param filename path to current xmir file in source directory
     * @return path to the modified [filename] file in temporary directory
     */
    private fun createTempDirectories(filename: String): String {
        val tmpPath = generateTmpPath(filename)
        val forDirs = File(tmpPath.substringBeforeLast(sep)).toPath()
        Files.createDirectories(forDirs)
        val newFilePath = Path.of(tmpPath)
        try {
            Files.createFile(newFilePath)
        } catch (e: Exception) {
            _logger.error(e.message)
        }
        return tmpPath
    }

    private fun generateTmpPath(filename: String) : String {
        val strPath = path.toString()
        return if (_gather) {
                "${strPath.substringBeforeLast(sep)}${sep}TMP${sep}${strPath.substringAfterLast(sep)}_tmp${filename.substring(strPath.length)}"
            } else {
                "${strPath.substringBeforeLast(sep)}${sep}${strPath.substringAfterLast(sep)}_$_postfix${filename.substring(strPath.length)}"
            }
    }
}