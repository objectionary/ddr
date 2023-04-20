package org.objectionary.ddr.launch

import org.objectionary.ddr.graph.AttributesSetter
import org.objectionary.ddr.graph.CondAttributesSetter
import org.objectionary.ddr.graph.GraphBuilder
import org.objectionary.ddr.graph.InnerPropagator
import org.objectionary.ddr.sources.SrsTransformed
import org.objectionary.ddr.transform.XslTransformer
import org.objectionary.ddr.transform.impl.BasicDecoratorsResolver
import org.objectionary.ddr.transform.impl.CondNodesResolver

/**
 * Stores all the information from xmir files in the form of a graph. Launches various analysis or transformation steps
 * on the graph.
 *
 * @param path path to the directory to be analysed
 * @param postfix postfix of the resulting directory
 */
class DdrLaunched(path: String, postfix: String = "ddr") {
    /** @property documents all documents from analyzed directory */
    val documents = SrsTransformed(path, XslTransformer(), postfix, false).walk()

    /** @property graph decoration hierarchy graph of xmir files from analyzed directory */
    private val graph = GraphBuilder(documents).createGraph()

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
