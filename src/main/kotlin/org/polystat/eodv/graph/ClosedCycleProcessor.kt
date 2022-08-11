package org.polystat.eodv.graph

/**
 * Processes decoration cycles
 */
@Throws(IllegalStateException::class)
fun processClosedCycles(graph: Graph) {
    val reached: MutableMap<IGraphNode, Boolean> = mutableMapOf()
    graph.igNodes.forEach { reached[it] = false }
    graph.heads.forEach { dfsReachable(it, reached) }
    for (entry in reached) {
        if (!entry.value) {
            graph.heads.add(entry.key)
            traverseCycles(entry.key, reached)
        }
    }
    if (graph.igNodes.size != reached.filter { it.value }.size) {
        throw IllegalStateException(
            "Not all nodes of inheritance graph were reached.\n " +
                    "Graph size: ${graph.igNodes.size}, reached nodes: ${reached.filter { it.value }.size}"
        )
    }
}

private fun dfsReachable(
    node: IGraphNode,
    reached: MutableMap<IGraphNode, Boolean>
) {
    if (reached[node] == true) return
    else reached[node] = true
    node.children.forEach { dfsReachable(it, reached) }
}

private fun traverseCycles(
    node: IGraphNode,
    reached: MutableMap<IGraphNode, Boolean>
) {
    if (reached[node]!!) return
    reached[node] = true
    node.children.forEach { traverseCycles(it, reached) }
}
