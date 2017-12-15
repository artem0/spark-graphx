package com.github.graphx.pregel.ssp

import org.apache.spark.SparkContext
import org.apache.spark.graphx.util.GraphGenerators
import org.apache.spark.graphx.{Edge, Graph, VertexId}

/**
  * Solving Shortest path problem - https://en.wikipedia.org/wiki/Shortest_path_problem
  * with Pregel algorithm
  * @param sc SparkContext
  */
class ShortestPathProblem(sc: SparkContext) {

  /**
    * Graph with any type of data in a vertex and double as attribute type
    * VT - the vertex attribute type
    */
  type GenericGraph[VT] = Graph[VT, Double]

  def shortestPath[VT](graph: GenericGraph[VT], sourceId: VertexId): String = {
    val initialGraph = graph.mapVertices((id, _) =>
      if (id == sourceId) 0.0 else Double.PositiveInfinity)

    val sssp = initialGraph.pregel(Double.PositiveInfinity)(
      (_, dist, newDist) => math.min(dist, newDist),
      triplet => {
        //Distance accumulator
        if (triplet.srcAttr + triplet.attr < triplet.dstAttr) {
          Iterator((triplet.dstId, triplet.srcAttr + triplet.attr))
        } else {
          Iterator.empty
        }
      },
      (a, b) => math.min(a, b)
    )
    sssp.vertices.sortByKey(ascending = true).collect.mkString("\n")
  }

  /**
    *  Graph with strings in vertexes and specified distances for testing purposes
    * @return Graph object
    */
  def testGraph: GenericGraph[String] = {
    val vertices = sc.parallelize(Array(
      (1L, "one"), (2L, "two"),
      (3L, "three"), (4L, "four"))
    )

    val relationships =
      sc.parallelize(Array(
        Edge(1L, 2L, 1.0), Edge(1L, 4L, 2.0),
        Edge(2L, 4L, 3.0), Edge(3L, 1L, 1.0),
        Edge(3L, 4L, 5.0))
      )
    Graph(vertices, relationships)
  }

  /**
    * Random graph for testing purposes.
    * Generate a graph whose vertex out degree distribution is log normal.
    * @return Graph object
    */
  def randomGraph: GenericGraph[Long] =
    GraphGenerators.logNormalGraph(sc, numVertices = 100).mapEdges(e => e.attr.toDouble)

  def graphToString[T](graph: GenericGraph[T]): String ={
    import scala.compat.Platform.EOL
    s" Vertices:$EOL" + graph.vertices.collect.mkString(s"$EOL") +
      s"$EOL Edges:$EOL" + graph.edges.collect.mkString(s"$EOL")
  }
}
