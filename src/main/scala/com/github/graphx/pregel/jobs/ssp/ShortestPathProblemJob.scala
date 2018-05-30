package com.github.graphx.pregel.jobs.ssp

import com.github.graphx.pregel.ssp.ShortestPathProblem
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.graphx.VertexId

object ShortestPathProblemJob extends App {
  Logger.getLogger("org").setLevel(Level.ERROR)
  val sc = new SparkContext("local[*]", "ShortestPathProblemDemo")
  val ssp = new ShortestPathProblem(sc)

  val sourceIdForTest: VertexId = 3
  val sourceIdForRandom: VertexId = 75

  val testGraph = ssp.testGraph
  val resultOnTestGraph = ssp.shortestPath(testGraph, sourceIdForTest)
  println(s"Test Graph:\n${ssp.graphToString(testGraph)}\n\n" +
    s"Distances on the test graph $resultOnTestGraph\n")

  val randomGraph = ssp.randomGraph
  val resultOnRandomGraph = ssp.shortestPath(randomGraph, sourceIdForRandom)
  println(s"Distances on the random graph $resultOnRandomGraph\n")
}
