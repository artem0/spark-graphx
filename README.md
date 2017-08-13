# spark-graphx
Spark 2.0 GraphX Social graph analysis

Example of the usage Apache Spark for analysis of social graph of users - 
like social network with model friend to friend but with fake user names.

Social graph is represented in form user_id -> array of related user's ids,
file is stored in resources directory - UserGraph.txt

Tested data is stored in resource directory - UserNames.tsv - file in
format user_id -> user_name, necessary for joining with Graph of contacts.

Examples contains searching of:
 1) Most connected user in social graph based on graph degrees
 2) Degree of separation for single user based on [Breadth-first search](https://en.wikipedia.org/wiki/Breadth-first_search) with [Pregel](https://stanford.edu/~rezab/classes/cme323/S15/notes/lec8.pdf)
