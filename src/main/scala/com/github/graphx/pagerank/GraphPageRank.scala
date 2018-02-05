package com.github.graphx.pagerank

import com.github.graphx.pregel.social.SocialGraph
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.graphx.VertexRDD

import scala.compat.Platform.EOL

object GraphPageRank extends App {
  Logger.getLogger("org").setLevel(Level.ERROR)
  val sc = new SparkContext("local[*]", "GraphPageRankDemo")

  val socialGraph = new SocialGraph(sc)
  val tolerance = 0.0001

  /**
    * Dynamic implementation uses the Pregel interface and runs PageRank until convergence
    */
  val ranks: VertexRDD[Double] = socialGraph.graph.pageRank(tol = tolerance).vertices

  /**
    * The standalone Graph interface and runs PageRank for a fixed number of iterations
    */
  val static: VertexRDD[Double] = socialGraph.graph.staticPageRank(numIter = 20).vertices

  def handleResult(ranks: VertexRDD[Double]) = {
    socialGraph.verts.join(ranks).map {
      case (_, (username, rank)) => (username, rank)
    }.sortBy({ case (_, rank) => rank }, ascending = false).take(10)
  }

  val topUsersDynamically = handleResult(ranks).mkString(EOL)
  println(s"Top 10 users in network (counted with tolerance until convergence $tolerance):$EOL$topUsersDynamically$EOL")

  val topUsersIterative = handleResult(ranks).mkString(EOL)
  println(s"Top 10 users in network (counted iterative):$EOL$topUsersDynamically$EOL")
}
