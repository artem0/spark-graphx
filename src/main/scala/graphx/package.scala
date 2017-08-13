package object graphx {

  val USER_NAMES_FILE_NAME = "/UserNames.tsv"
  val USER_GRAPH_FILE_NAME = "/UserGraph.tsv"

  val USER_NAMES_FILE: String = getClass.getResource(USER_NAMES_FILE_NAME).getPath
  val USER_GRAPH_FILE: String = getClass.getResource(USER_GRAPH_FILE_NAME).getPath

}
