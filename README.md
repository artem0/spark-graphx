## Spark GraphX showcase
## Spark 2.0 GraphX Social graph analysis

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

Examples contains searching of:
 1) Most connected user in social graph based on graph degrees.
 2) Degree of separation for single user based on [Breadth-first search](https://en.wikipedia.org/wiki/Breadth-first_search)
 with [Pregel](https://stanford.edu/~rezab/classes/cme323/S15/notes/lec8.pdf).
 Input is user's id - output is list of tuple with users and degree of separation between them. 
 3) Degree of separation between two defined users, as degree of separation for the single user, it's
 based on Breadth first search with Pregel.
 Input is two user's ids - output is degree of separation between them. 

All examples you can find in `com.github.graphx.pregel.social.demo.SocialGraphDemo`, 
Launching with `SBT`:

`sbt runMain com.github.graphx.pregel.social.demo.SocialGraphDemo`
