/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Olesia Subbotina
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
 * Transforms xml file using provided xsl
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
