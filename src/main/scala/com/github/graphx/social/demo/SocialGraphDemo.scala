package com.github.graphx.social.demo

import com.github.graphx.social.SocialGraph
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext

object SocialGraphDemo {

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    val sc = new SparkContext("local[*]", "GraphX")

    val graph = new SocialGraph(sc)

    println("Top 10 most-connected users:")
    graph.getMostConnectedUsers(10) foreach println

    println("Computing degrees of separation for user Arch")
    graph.degreeOfSeparationSingleUser(5306) foreach println

    println("Computing degrees of separation for user Arch and Fred")
    graph.degreeOfSeparationTwoUser(5306, 14) foreach println
  }
}