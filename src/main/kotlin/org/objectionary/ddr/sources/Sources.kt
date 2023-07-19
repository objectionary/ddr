package org.objectionary.ddr.sources

import org.objectionary.ddr.transform.XslTransformer
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
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
 */
class SrsTransformed(
    strPath: String,
    private val transformer: XslTransformer,
    private val postfix: String,
) : Sources {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    /** @property inPath path to directory with source files or single source file */
    val inPath: Path = Path.of(strPath)

    /** @property resPath path to directory with the result */
    val resPath: Path = Path.of("${inPath}_$postfix")

    /** @property documents all documents */
    private val documents: MutableMap<Document, String> = mutableMapOf()

    /**
     * Walks through [inPath], make some xsl transformation on xmir files using [transformer] and collect [documents]
     *
     * @return [MutableMap] with collected [documents] as key and their filenames as value
     */
    override fun walk(): MutableMap<Document, String> {
        Files.walk(inPath)
            .filter(Files::isRegularFile)
            .forEach {
                val result = createResultDirectories(it.toString())
                transformer.transformXml(it.toString(), result)
                documents[getDocument(result)!!] = result
            }
        return documents
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
        } catch (e: FileNotFoundException) {
            logger.error(e.printStackTrace().toString())
            return null
        }
    }

    /**
     * Creates a new temporary directory for transformed xmir files
     *
     * @param filename path to current xmir file in source directory
     * @return path to the temporary directory with xmir files
     *
     * @todo #119:30min if output directory already exists createFile throws exception and logger prints error message. This situation should be handled correctly.
     */
    private fun createResultDirectories(filename: String): String {
        val tmpPath = generateResultPath(filename)
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
     * Generates path to result files
     *
     * @param filename path to current xmir file in source directory
     * @return path to the result files
     *
     */
    private fun generateResultPath(filename: String): String {
        return "$resPath${filename.substring(inPath.toString().length)}"
    }
}
