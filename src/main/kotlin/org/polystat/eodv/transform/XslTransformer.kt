package org.polystat.eodv.transform

import mu.KotlinLogging
import java.io.File
import java.io.FileNotFoundException
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * Transforms xml with provided xsl
 * Usage:
    transformer.createXsl(
        "src\\launch.main\\resources\\sample_in\\xml\\in1.xml",
        "src\\launch.main\\resources\\sample_out\\out1.xml",
        "src\\launch.main\\resources\\sample_in\\xsl\\ins1.xsl"
    )
 */
class XslTransformer {
    private val logger = KotlinLogging.logger {}

    fun createXsl(
        inFilename: String,
        outFilename: String,
        xslFilename: String
    ) {
        try {
            val factory = TransformerFactory.newInstance()
            val transformer = factory.newTemplates(StreamSource(File(xslFilename).inputStream())).newTransformer()
            val source = StreamSource(File(inFilename).inputStream())
            val result = StreamResult(File(outFilename).outputStream())
            transformer.transform(source, result)
        } catch (e: Exception) {
            when (e) {
                is FileNotFoundException, is TransformerConfigurationException -> logger.error { e.message }
                is TransformerException -> {
                    val locator = e.locator
                    logger.error { "Error in col:${locator.columnNumber} line:${locator.lineNumber}" }
                    logger.error { e.exception.message }
                }
                else -> throw e
            }
        }
    }
}
