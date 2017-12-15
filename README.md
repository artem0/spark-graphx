## Spark 2.0 GraphX - Pregel

## Pregel

### Social graph analysis
Example of the usage Apache Spark for analysis of social graph of users - 
like social network with model friend to friend with fake user names.
Also you can consider this example in terms of network theory like p2p with substitution of users
on the specific values.

Social graph is represented in the next form: `one user -> multiple friends` 
`Datasets` has the next structure: `user_id array of related user's ids`,
file is stored in resources directory - `UserGraph.txt`. For example, user with id `5988` has 
friends with ids `748 1722 3752` will be represented in pretty obvious form: 
`5988 748 1722 3752`

Tested data is stored in resource directory - `UserNames.tsv` - file in
format `user_id -> user_name`, necessary for joining with graph of contacts.
It can be treated as a simple tuple - id from `UserGraph.txt` and name of vertex of graph.
Names of user are random, any coincidence  are accidental.

Manipulations with a social graph:
 1) Most connected user in social graph based on graph degrees.
 2) Degree of separation for single user based on [Breadth-first search](https://en.wikipedia.org/wiki/Breadth-first_search)
 with [Pregel](https://stanford.edu/~rezab/classes/cme323/S15/notes/lec8.pdf).
 Input is user's id - output is list of tuple with users and degree of separation between them. 
 3) Degree of separation between two defined users, as degree of separation for the single user, it's
 based on Breadth first search with Pregel.
 Input is two user's ids - output is degree of separation between them. 
 4) [Connected component](https://en.wikipedia.org/wiki/Connected_component_(graph_theory)) for social
 graph - under the hood it delegates to Pregel.

Example with social graph you can find in `com.github.graphx.pregel.social.demo.SocialGraphDemo`, 
Launching with `SBT`:

`sbt runMain com.github.graphx.pregel.social.demo.SocialGraphDemo`

### Shortest path problem with Dijkstra’s algorithm
In graph theory, the [shortest path problem](https://en.wikipedia.org/wiki/Shortest_path_problem) is the problem of 
finding a path between two vertices (or nodes) in a graph such that the sum of the weights of its constituent edges 
is minimized.

This project solves `shortest path problem` with [Dijkstra's 
algorithm](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm) with relying on **Pregel algorithm** for
propagating messages.

The next code snapshot demonstrates how easy you can implement similar algorithms:

```scala
initialGraph.pregel(Double.PositiveInfinity)(
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
```

Launching with `SBT`:

`sbt runMain com.github.graphx.pregel.ssp.demo.ShortestPathProblemDemo`

### Licence: GNU General Public License v3.0
