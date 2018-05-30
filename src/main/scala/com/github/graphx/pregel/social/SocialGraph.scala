package com.github.graphx.pregel.social

import org.apache.spark._
import org.apache.spark.graphx.{Graph, _}
import org.apache.spark.rdd.RDD

class SocialGraph(sc: SparkContext) {
  type ConnectedUser = (PartitionID, String)
  type DegreeOfSeparation = (Double, String)

  def verts: RDD[(VertexId, String)] = sc.textFile(USER_NAMES).flatMap(InputDataFlow.parseNames)

  def edges: RDD[Edge[PartitionID]] = sc.textFile(USER_GRAPH).flatMap(InputDataFlow.makeEdges)

  /**
    * Build social graph from verts and edges
    * stored in tsv files
    * @return build graph
    */
  def graph = Graph(verts, edges).cache()

  /**
    * Find most connected user graph.degrees
    * @param amount threshold for returning first n user
    * @return most connected user in social graph
    */
  def getMostConnectedUsers(amount: Int): Array[(VertexId, ConnectedUser)] = {
    graph.degrees.join(verts)
      .sortBy({ case (_, (userName, _)) => userName }, ascending = false)
      .take(amount)
  }

  /**
    * Represent breadth-first search statement of social graph
    * via delegation to Pregel algorithm starting from the edge root
    * @param root The point of departure in BFS
    * @return breadth-first search statement
    */
  private def getBFS(root: VertexId) = {
    val initialGraph = graph.mapVertices((id, _) =>
      if (id == root) 0.0 else Double.PositiveInfinity)

    val bfs = initialGraph.pregel(Double.PositiveInfinity, maxIterations = 10)(
      (_, attr, msg) => math.min(attr, msg),
      triplet => {
        if (triplet.srcAttr != Double.PositiveInfinity) {
          Iterator((triplet.dstId, triplet.srcAttr + 1))
        } else {
          Iterator.empty
        }
      },
      (a, b) => math.min(a, b)).cache()
    bfs
  }

  /**
    * Degree of separation for the single user
    * as adapter to getBfs
    * @param root The point of departure in BFS
    * @return Degree of separation for the user
    */
  def degreeOfSeparationSingleUser(root: VertexId): Array[(VertexId, DegreeOfSeparation)] = {
    getBFS(root).vertices.join(verts).take(100)
  }

  /**
    * Degree of separation between two user
    * @param firstUser  VertexId for the first user
    * @param secondUser VertexId for the second user
    * @return Degree of separation for the users
    */
  def degreeOfSeparationTwoUser(firstUser: VertexId, secondUser: VertexId) = {
    getBFS(firstUser)
      .vertices
      .filter { case (vertexId, _) => vertexId == secondUser }
      .collect.map { case (_, degree) => degree }
  }

  /**
    * Compute the connected component membership of each vertex,
    * id of component is the lowest vertex id in a certain component
    * @return Tuple vertex id - lowest vertex id in a component
    */
  def connectedComponent = graph.connectedComponents().vertices

  /**
    * Compute the connected component with join in usernames
    */
  def connectedComponentGroupedByUsers =
    verts.join(connectedComponent).map {
      case (_, (username, comp)) => (username, comp)
    }

  /**
    * @return Number of triangles passing through each vertex.
    */
  def socialGraphTriangleCount = graph.triangleCount()
}