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
 * Sources that can be traversed
 */
interface Sources {
    /**
     * Walks through sources and collect documents in [MutableMap]
     *
     * @return [MutableMap] with collected documents as key and their filenames as value
     */
    fun walk(): MutableMap<Document, String>
}

/**
 * Source xmir files that are stored in [Document] format
 *
 * @param strPath string path to directory with source files or single source file
 * @property transformer the object with which xsl transformations on xmir files will be performed
 * @property postfix postfix of the resulting directory
 * @property gather if outputs should be gathered
 *
 * todo #108:60min remove an unnecessary parameter gather that is used only in tests (also fix the tests)
 */
class SrsTransformed(
    strPath: String,
    private val transformer: XslTransformer,
    private val postfix: String = "ddr",
    private val gather: Boolean = true
) : Sources {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    /** @property path path to directory with source files or single source file */
    val path: Path = Path.of(strPath)

    /** @property documents all documents */
    val documents: MutableMap<Document, String> = mutableMapOf()

    /**
     * Walks through [path], make some xsl transformation on xmir files using [transformer] and collect [documents]
     *
     * @return [MutableMap] with collected [documents] as key and their filenames as value
     */
    override fun walk(): MutableMap<Document, String> {
        Files.walk(path)
            .filter(Files::isRegularFile)
            .forEach {
                val tmpPath = createTempDirectories(it.toString())
                transformer.transformXml(it.toString(), tmpPath)
                documents[getDocument(tmpPath)!!] = tmpPath
            }
        return documents.toMutableMap()
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
