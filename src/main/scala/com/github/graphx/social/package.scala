package com.github.graphx

package object social {

  val USER_NAMES: String = getClass.getResource("/UserNames.tsv").getPath
  val USER_GRAPH: String = getClass.getResource("/UserGraph.tsv").getPath
}
