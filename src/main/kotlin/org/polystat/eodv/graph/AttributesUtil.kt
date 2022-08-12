package org.polystat.eodv.graph

import org.w3c.dom.Node

fun abstract(node: Node?) = node?.attributes?.getNamedItem("abstract")

fun name(node: Node?) = node?.attributes?.getNamedItem("name")?.textContent

fun base(node: Node?) = node?.attributes?.getNamedItem("base")?.textContent

fun ref(node: Node?) = node?.attributes?.getNamedItem("ref")?.textContent

fun line(node: Node?) = node?.attributes?.getNamedItem("line")?.textContent