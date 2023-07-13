package org.objectionary.ddr.launch

import org.objectionary.ddr.graph.AttributesSetter
import org.objectionary.ddr.graph.CondAttributesSetter
import org.objectionary.ddr.graph.GraphBuilder
import org.objectionary.ddr.graph.InnerPropagator
import org.objectionary.ddr.sources.SrsTransformed
import org.objectionary.ddr.transform.XslTransformer
import org.objectionary.ddr.transform.impl.BasicDecoratorsResolver
import org.objectionary.ddr.transform.impl.CondNodesResolver
import org.w3c.dom.Document

/**
 * Workflow of some software application.
 */
interface Workflow {
    /**
     * Starts the entire workflow.
     */
    fun launch()
}

/**
 * Stores all the information from xmir files in the form of a graph. Launches various analysis or transformation steps
 * on this graph.
 *
 * @property documents all documents from analyzed directory
 */
abstract class XmirAnalysisWorkflow(val documents: MutableMap<Document, String>) : Workflow {
    /** @property graph decoration hierarchy graph of xmir files from analyzed directory */
    protected val graph = GraphBuilder(documents).createGraph()

    /**
     * Aggregates all steps of analysis
     */
    abstract override fun launch()
}

/**
 * Stores all the information from xmir files in the form of a graph. Launches analysis and transformation steps for
 * the Dynamic Dispatch Removal on this graph.
 *
 * @param documents all documents from analyzed directory
 */
class DdrWorkflow(documents: MutableMap<Document, String>) : XmirAnalysisWorkflow(documents) {
    /**
     * Constructs [documents] from [path]
     *
     * @param path path to the directory to be analysed
     * @param postfix postfix of the resulting directory
     */
    constructor(path: String, postfix: String = "ddr") : this(
        SrsTransformed(path, XslTransformer(), postfix).walk())

    /**
     * Aggregates all steps of Dynamic Dispatch Removal
     */
    override fun launch() {
        CondAttributesSetter(graph).processConditions()
        AttributesSetter(graph).setAttributes()
        InnerPropagator(graph).propagateInnerAttrs()
        CondNodesResolver(graph, documents).resolve()
        BasicDecoratorsResolver(graph, documents).resolve()
    }
}
