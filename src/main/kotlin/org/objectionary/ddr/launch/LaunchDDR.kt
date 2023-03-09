package org.objectionary.ddr.launch

import org.objectionary.ddr.graph.repr.Graph
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import java.io.File

private val sep = File.separatorChar

class LaunchDDR(private val path: String, private val postfix: String = "ddr") {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    private val documents: MutableMap<Document, String> = mutableMapOf()
    private val graph = Graph()
}