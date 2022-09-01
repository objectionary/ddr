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

package org.objectionary.ddr.transform

import com.jcabi.xml.XML
import com.jcabi.xml.XMLDocument
import com.yegor256.xsline.TrClasspath
import com.yegor256.xsline.Xsline
import org.eolang.parser.ParsingTrain
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Transforms xml file using provided xsl
 */
class XslTransformer {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

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
        val xmir: XML = XMLDocument(File(inFilename))
        val after = Xsline(
            TrClasspath(
                ParsingTrain().empty(),
                "/org/eolang/parser/add-refs.xsl",
                "/org/eolang/parser/expand-aliases.xsl",
                "/org/eolang/parser/resolve-aliases.xsl"
            ).back()
        ).pass(xmir)
        File(outFilename).outputStream().write(after.toString().toByteArray())
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
        val xmir: XML = XMLDocument(File(inFilename))
        val after = Xsline(
            TrClasspath(
                ParsingTrain().empty(),
                xslMod
            ).back()
        ).pass(xmir)
        File(outFilename).outputStream().write(after.toString().toByteArray())
    }
}
