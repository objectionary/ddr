/**
 * Header??
 */

package org.polystat.eodv.launch

import org.polystat.eodv.graph.GraphBuilder

fun main(args: Array<String>) {
    val builder = GraphBuilder()
    builder.createGraph("src/test/resources/graph/in/double_hie.xml")
}