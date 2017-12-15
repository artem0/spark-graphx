package com.github.graphx.pregel.social

import org.apache.spark._
import org.apache.spark.graphx.{Graph, _}
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ListBuffer

object InputDataFlow {

  def parseNames(line: String): Option[(VertexId, String)] = {
    val fields = line.split('\t')
    if (fields.length > 1)
      Some(fields(0).trim().toLong, fields(1))
    else None
  }

  def makeEdges(line: String): List[Edge[Int]] = {
    var edges = new ListBuffer[Edge[Int]]()
    val fields = line.split(" ")
    val origin = fields(0)
    (1 until fields.length)
      .foreach { p =>
        edges += Edge(origin.toLong, fields(p).toLong, 0)
      }
    edges.toList
  }

}

class SocialGraph(sc: SparkContext) {
  type ConnectedUser = (PartitionID, String)
  type DegreeOfSeparation = (Double, String)

  private def verts: RDD[(VertexId, String)] = sc.textFile(USER_NAMES).flatMap(InputDataFlow.parseNames)

  private def edges: RDD[Edge[PartitionID]] = sc.textFile(USER_GRAPH).flatMap(InputDataFlow.makeEdges)

  /**
    * Build social graph from verts and edges
    * stored in tsv files
    * @return build graph
    */
  private def graph = Graph(verts, edges).cache()

  /**
    * Find most connected user graph.degrees
    * @param amount threshold for returning first n user
    * @return most connected user in social graph
    */
  def getMostConnectedUsers(amount: Int): Array[(VertexId, ConnectedUser)] = {
    graph.degrees.join(verts)
      .sortBy({ case ((_, (userName, _))) => userName }, ascending = false)
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
}