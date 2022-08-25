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
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * Transforms xml file using provided xsl
 */
class XslTransformer {
    private val logger = KotlinLogging.logger(this.javaClass.name)
    private val sep = File.separatorChar
    private val addRefsXsl = "src${sep}main${sep}resources${sep}add-refs.xsl"
    private val expandAliasesXsl = "src${sep}main${sep}resources${sep}expand-aliases.xsl"
    private val resolveAliasesXsl = "src${sep}main${sep}resources${sep}resolve-aliases.xsl"

    /**
     * Creates a new xml by applying several xsl transformations to it
     *
     * @param inFilename to the input file
     * @param outFilename path to the output file
     */
    fun transformXml(
        inFilename: String,
        outFilename: String
    ) {
        singleTransformation(inFilename, "tmp1", addRefsXsl)
        singleTransformation("tmp1", "tmp2", expandAliasesXsl)
        singleTransformation("tmp2", outFilename, resolveAliasesXsl)
    }

    /**
     * Perform single transformation of the provided [inFilename]
     *
     * @param inFilename input file location
     * @param outFilename output file location
     * @param xslMod path to the xsl file with modification rules
     */
    fun singleTransformation(
        inFilename: String,
        outFilename: String,
        xslMod: String
    ) {
        try {
            val factory = TransformerFactory.newInstance()
            val transformer = factory.newTemplates(StreamSource(File(xslMod).inputStream())).newTransformer()
            val source = StreamSource(File(inFilename).inputStream())
            val os = File("tmp3")
            val tmp2res = StreamResult(os.outputStream())
            transformer.transform(source, tmp2res)
            File(outFilename).writeBytes(os.readBytes())
        } catch (e: Exception) {
            logger.error { e.printStackTrace() }
        }
    }
}
