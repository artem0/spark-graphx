package com.github.graphx.pagerank

import com.github.graphx.pregel.social.SocialGraph
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.graphx.VertexRDD

object SocialPageRankJob {

  /**
    * Dynamic implementation uses the Pregel interface and runs PageRank until convergence
    */
  def ranks(socialGraph: SocialGraph, tolerance: Double): VertexRDD[Double] =
    socialGraph.graph.pageRank(tol = tolerance).vertices

  /**
    * The standalone Graph interface and runs PageRank for a fixed number of iterations
    */
  def static(socialGraph: SocialGraph, tolerance: Double): VertexRDD[Double] =
    socialGraph.graph.staticPageRank(numIter = 20).vertices

  def handleResult(socialGraph: SocialGraph, ranks: VertexRDD[Double]) = {
    socialGraph.verts.join(ranks).map {
      case (_, (username, rank)) => (username, rank)
    }.sortBy({ case (_, rank) => rank }, ascending = false).take(10)
  }

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    val sc = new SparkContext("local[*]", "PageRank")

    val socialGraph: SocialGraph = new SocialGraph(sc)
    val TOLERANCE: Double = 0.0001

    import scala.compat.Platform.{EOL => D}
    val topUsersDynamically = handleResult(socialGraph, ranks(socialGraph, TOLERANCE)).mkString(D)
    val topUsersIterative = handleResult(socialGraph, static(socialGraph, TOLERANCE)).mkString(D)

    println(s"Top 10 users in network counted with TOLERANCE until convergence $TOLERANCE - $D $topUsersDynamically")
    println(s"Top 10 users in the network counted iteratively - $D $topUsersIterative")

    sc.stop()
  }
}
