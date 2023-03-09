package org.objectionary.ddr.sources

import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import java.io.File

private val sep = File.separatorChar

interface Sources {
    val path: String //maybe make it private?
    val documents : MutableMap<Document, String>

    fun walkSources()
}

class SourcesDDR(override val path: String, private val postfix: String = "ddr") : Sources{
    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    override val documents: MutableMap<Document, String> = mutableMapOf()

    override fun walkSources() {
        TODO()
    }
}