package graphx

import org.apache.spark._
import org.apache.spark.graphx.{Graph, _}

object InputDataFlow {

  def parseNames(line: String): Option[(VertexId, String)] = {
    val fields = line.split('\t')
    if (fields.length > 1)
      Some(fields(0).trim().toLong, fields(1))
    else None
  }

  def makeEdges(line: String) : List[Edge[Int]] = {
    import scala.collection.mutable.ListBuffer
    var edges = new ListBuffer[Edge[Int]]()
    val fields = line.split(" ")
    val origin = fields(0)
    (1 until fields.length)
      .foreach { p => edges += Edge(origin.toLong, fields(p).toLong, 0) }
    edges.toList
  }

}

class GraphX(sc: SparkContext) {
  private def verts = sc.textFile(USER_NAMES_FILE).flatMap(InputDataFlow.parseNames)

  private def edges = sc.textFile(USER_GRAPH_FILE).flatMap(InputDataFlow.makeEdges)

  private def graph = Graph(verts, edges).cache()


  def getMostConnectedUsers(amount:Int): Array[(VertexId, (PartitionID, String))] = {
    graph.degrees.join(verts)
      .sortBy( {case( (_, (userName, _))) => userName }, ascending=false ).take(amount)
  }

  private def getBfs(root:VertexId) = {
    val initialGraph = graph.mapVertices((id, _) => if (id == root) 0.0 else Double.PositiveInfinity)

    val bfs = initialGraph.pregel(Double.PositiveInfinity, 10)(
      (_, attr, msg) => math.min(attr, msg),
      triplet => {
        if (triplet.srcAttr != Double.PositiveInfinity) {
          Iterator((triplet.dstId, triplet.srcAttr+1))
        } else {
          Iterator.empty
        }
      },
      (a,b) => math.min(a,b)).cache()
    bfs
  }

  def degreeOfSeparationSingleUser(root:VertexId) = {
    getBfs(root).vertices.join(verts).take(100)
  }

  def degreeOfSeparationTwoUser(firstUser:VertexId, secondUser:VertexId ) = {
    getBfs(firstUser)
      .vertices
      .filter{case (vertexId, _) => vertexId == secondUser}
      .collect
  }
}