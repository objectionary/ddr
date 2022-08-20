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
    private val sep = File.separatorChar
    private val ADD_REFS = "src${sep}main${sep}resources${sep}add-refs.xsl"
    private val RESOLVE_ALIASES = "src${sep}main${sep}resources${sep}resolve-aliases.xsl"
    private val EXPAND_ALIASES = "src${sep}main${sep}resources${sep}expand-aliases.xsl"

    fun createXsl(
        inFilename: String,
        outFilename: String
    ) {
        try {
            val factory1 = TransformerFactory.newInstance()
            val transformer1 = factory1.newTemplates(StreamSource(File(ADD_REFS).inputStream())).newTransformer()
            val source1 = StreamSource(File(inFilename).inputStream())
            val os1 = File("tmp1")
            val tmp1res = StreamResult(os1.outputStream())
            transformer1.transform(source1, tmp1res)

            val factory2 = TransformerFactory.newInstance()
            val transformer2 = factory2.newTemplates(StreamSource(File(EXPAND_ALIASES).inputStream())).newTransformer()
            val source2 = StreamSource(os1.inputStream())
            val os2 = File("tmp2")
            val tmp2res = StreamResult(os2.outputStream())
            transformer2.transform(source2, tmp2res)

            val factory3 = TransformerFactory.newInstance()
            val transformer3 = factory3.newTemplates(StreamSource(File(RESOLVE_ALIASES).inputStream())).newTransformer()
            val source3 = StreamSource(os2.inputStream())
            val result = StreamResult(File(outFilename).outputStream())
            transformer3.transform(source3, result)
        } catch (e: Exception) {
            when (e) {
                is FileNotFoundException, is TransformerConfigurationException -> logger.error { e.printStackTrace() }
                is TransformerException -> {
                    logger.error { e.printStackTrace() }
                }
                else -> throw e
            }
        }
    }

    fun singleTransformation(
        inFilename: String,
        outFilename: String,
        xslMod: String
    ) {
        try {
            val factory = TransformerFactory.newInstance()
            val transformer = factory.newTemplates(StreamSource(File(xslMod).inputStream())).newTransformer()
            val source = StreamSource(File(inFilename).inputStream())
            val os2 = File("tmp2")
            val tmp2res = StreamResult(os2.outputStream())
            transformer.transform(source, tmp2res)
            File(outFilename).writeBytes(os2.readBytes())
        } catch (e: Exception) {
            when (e) {
                is FileNotFoundException, is TransformerConfigurationException -> logger.error { e.printStackTrace() }
                is TransformerException -> {
                    logger.error { e.printStackTrace() }
                }
                else -> throw e
            }
        }
    }
}
