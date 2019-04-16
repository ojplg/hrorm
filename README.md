# <a href="http://hrorm.org">hrorm</a>


Hrorm is a concise, declarative, opinionated, type-checked library for the creation of Data Access 
Objects (DAOs) that will not inflict your codebase with XMLosis or annotationitis.

The <a href="http://hrorm.org">hrorm</a> website contains documentation for using
it as well as detailed descriptions of the hows and whys of hrorm and its ethos.

### Hacking

There is not much to say here.
The code builds with maven.
Hrorm itself has no dependencies.
The tests have a few dependencies, but they are all simply jars.
The tests run against an in memory database (H2).
There is not much code.
Once you clone it, you're basically ready to go.

Hrorm requires Java 8.

Very short [CONTRIBUTING](CONTRIBUTING.md) doc.

You can also run the tests against postgres, but it takes a bit of work.
1. Set up postgres locally
   1. User named "hrorm_user"
   1. Password "hrorm_password"
   1. "hrorm_user" must own a database named "hrorm"
1. cd to the scripts directory and run the `test_postgres.sh` script or edit the `HelperFactory` to use the `PostgresHelper` instead of the `H2Helper`

### Improvement Ideas and Questions

* Where object improvements:
    * Is creating strange statements like "a OR b AND c" desirable? Should it be prohibited somehow?
    * Can we use the column types (or field types) to improve type checking when building where clauses?
    * Support for columns on joined tables?
    * Support for columns on child tables?
* Hrorm does a lot of string building at query time. Should SQL strings be cached? 
* Similarly, DaoBuilder objects are always mutable. Perhaps they should lock at Dao creation time?
* Support different types, e.g. String GUIDs, for primary keys
* Add methods that allow for updates and deletes based on Where objects
