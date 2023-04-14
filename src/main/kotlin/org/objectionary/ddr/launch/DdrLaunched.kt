package org.objectionary.ddr.launch

import org.objectionary.ddr.graph.AttributesSetter
import org.objectionary.ddr.graph.CondAttributesSetter
import org.objectionary.ddr.graph.GraphBuilder
import org.objectionary.ddr.graph.InnerPropagator
import org.objectionary.ddr.graph.repr.Graph
import org.objectionary.ddr.sources.SrsTransformed
import org.objectionary.ddr.transform.XslTransformer
import org.objectionary.ddr.transform.impl.BasicDecoratorsResolver
import org.objectionary.ddr.transform.impl.CondNodesResolver
import org.slf4j.LoggerFactory

/**
 * Stores all the information from xmir files in the form of a graph. Launches various analysis or transformation steps
 * on the graph.
 *
 * @param path path to the directory to be analysed
 * @param postfix postfix of the resulting directory
 * @todo #108:120min implement launcher class
 */
class DdrLaunched(path: String, postfix: String = "ddr") {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    private val documents = SrsTransformed(path, XslTransformer(), postfix, false).walk()
    private val graph: Graph

    init {
        val builder = GraphBuilder(documents)
        builder.createGraph()
        graph = builder.graph
    }

    /**
     * Aggregates all steps of analysis
     */
    fun launch() {
        CondAttributesSetter(graph).processConditions()
        AttributesSetter(graph).setAttributes()
        InnerPropagator(graph).propagateInnerAttrs()
        CondNodesResolver(graph, documents).resolve()
        BasicDecoratorsResolver(graph, documents).resolve()
    }
}
