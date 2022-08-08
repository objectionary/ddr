package org.polystat.eodv.graph

class CycleDetector(val graph: Graph) {
    val initiallyReachable: MutableSet<IGraphNode> = mutableSetOf()

    fun findInitiallyReachable() {
        val reached: MutableMap<IGraphNode, Boolean> = mutableMapOf()
        graph.igNodes.forEach { reached[it] = false }
        graph.heads.forEach { dfsReachable(it, reached) }
        println("SIZES: ${graph.igNodes.size} AND REACHED ${reached.filter { it.value }.size}")
//        assert(graph.igNodes.size == reached.filter { it.value }.size)
        for (entry in reached) {
            if (!entry.value) {
                graph.heads.add(entry.key)
                traverseCycles(entry.key, reached)
            }
        }
        assert(graph.igNodes.size == reached.filter { it.value }.size)
    }

    private fun dfsReachable(node: IGraphNode, reached: MutableMap<IGraphNode, Boolean>) {
        if (reached[node] == true) return
        else reached[node] = true
        node.children.forEach { dfsReachable(it, reached) }
    }

    private fun traverseCycles(node: IGraphNode, reached: MutableMap<IGraphNode, Boolean>) {
        if (reached[node]!!) return
        reached[node] = true
        node.children.forEach { traverseCycles(it, reached) }
    }
}