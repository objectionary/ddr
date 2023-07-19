package org.objectionary.ddr.sources

import org.objectionary.ddr.transform.XslTransformer
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import java.io.File
import java.io.FileInputStream
import java.io.IOException
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
    fun walk(): MutableMap<Document, Path>
}

/**
 * Source xmir files that are stored in [Document] format
 *
 * @property inPath path to directory with source files or single source file
 * @property transformer the object with which xsl transformations on xmir files will be performed
 * @property postfix postfix of the resulting directory
 */
class SrsTransformed(
    val inPath: Path,
    private val transformer: XslTransformer,
    private val postfix: String,
) : Sources {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    /** @property resPath path to directory with the result */
    val resPath: Path = Path.of("${inPath}_$postfix")

    /** @property documents all documents */
    private val documents: MutableMap<Document, Path> = mutableMapOf()

    /**
     * Walks through [inPath], make some xsl transformation on xmir files using [transformer] and collect [documents]
     *
     * @return [MutableMap] with collected [documents] as key and their filenames as value
     */
    override fun walk(): MutableMap<Document, Path> {
        Files.walk(inPath)
            .filter(Files::isRegularFile)
            .forEach {
                val result = createResultDirectories(it)
                transformer.transformXml(it.toString(), result.toString())
                documents[getDocument(result)] = result
            }
        return documents
    }

    /**
     * Get Document from source xml file
     *
     * @param path path to source xml file
     * @return generated [Document]
     */
    private fun getDocument(path: Path): Document {
        FileInputStream(path.toFile()).use { stream ->
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
        }
    }

    /**
     * Creates a new temporary directory for transformed xmir files
     *
     * @param path path to current xmir file in source directory
     * @return path to the temporary directory with xmir files
     *
     * @todo #119:30min if output directory already exists createFile throws exception and logger prints error message. This situation should be handled correctly.
     */
    private fun createResultDirectories(path: Path): Path {
        val tmpPath = "$resPath${path.toString().substring(inPath.toString().length)}"
        val forDirs = File(tmpPath.substringBeforeLast(sep)).toPath()
        Files.createDirectories(forDirs)
        val newFilePath = Path.of(tmpPath)
        try {
            Files.createFile(newFilePath)
        } catch (e: IOException) {
            logger.error(e.message)
        }
        return Path.of(tmpPath)
    }
}
