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

/**
 * Stores the source files in [Document] format and makes some changes to them
 */
interface Sources {
    /** @property path path to directory with source files or single source file */
    val path: Path

    /** @property documents all documents */
    val documents: MutableMap<Document, String>

    /**
     * Walks through [path] and collect [documents]
     */
    fun walkSources()
}

/**
 * Stores the source xmir files in [Document] format and makes some changes to them
 *
 * @param strPath string path to directory with source files or single source file
 * @param postfix postfix of the resulting directory
 * @param gather if outputs should be gathered
 */
class SourcesDdr(
    strPath: String,
    private val postfix: String = "ddr",
    private val gather: Boolean = true
) : Sources {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    /** @property path path to directory with source files or single source file */
    override val path: Path = Path.of(strPath)

    /** @property documents all documents */
    override val documents: MutableMap<Document, String> = mutableMapOf()

    /**
     * Walks through [path], make some xml transformation on xmir files and collect [documents]
     */
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
     * @return generated [Document]
     */
    private fun getDocument(filename: String): Document? {
        try {
            val factory = DocumentBuilderFactory.newInstance()
            FileInputStream(filename).use { return factory.newDocumentBuilder().parse(it) }
        } catch (e: Exception) {
            logger.error(e.printStackTrace().toString())
            return null
        }
    }

    /**
     * Creates a new temporary directory for transformed xmir files
     *
     * @param filename path to current xmir file in source directory
     * @return path to the temporary directory with xmir files
     */
    private fun createTempDirectories(filename: String): String {
        val tmpPath = generateTmpPath(filename)
        val forDirs = File(tmpPath.substringBeforeLast(sep)).toPath()
        Files.createDirectories(forDirs)
        val newFilePath = Path.of(tmpPath)
        try {
            Files.createFile(newFilePath)
        } catch (e: Exception) {
            logger.error(e.message)
        }
        return tmpPath
    }

    /**
     * Generates path to temporary directories
     *
     * @param filename path to current xmir file in source directory
     * @return path to the temporary directories
     */
    private fun generateTmpPath(filename: String): String {
        val strPath = path.toString()
        return if (gather) {
            "${strPath.substringBeforeLast(sep)}${sep}TMP$sep${strPath.substringAfterLast(sep)}_tmp${filename.substring(strPath.length)}"
        } else {
            "${strPath.substringBeforeLast(sep)}$sep${strPath.substringAfterLast(sep)}_$postfix${filename.substring(strPath.length)}"
        }
    }
}
