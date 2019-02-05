# <a href="http://hrorm.org">hrorm</a>


Hrorm is a concise, declarative, opinionated, type-checked library for the creation of Data Access 
Objects (DAOs) that will not inflict your codebase with XMLosis or annotationitis.

The <a href="http://hrorm.org">hrorm</a> website contains documentation for using
it as well as detailed descriptions of the hows and whys of hrorm and its ethos.

### Hacking

There is not much to say here.
The code builds with maven.
Hrorm itself has no dependencies.
To build and run the tests requires only a few things and the tests run against 
an in-memory database.
There is not much code.
Once you clone it, you're basically ready to go.

Hrorm requires Java 8.

### Improvement Ideas

* Expose fields in addition to columns in select statements (either set on columns at
create time, or derive from get/set method names)
* Add support for more Java types
* Perhaps add a KeylessDaoBuilder: but need to think about how best to share code among 
all the DAO builder classes while still supporting the clean types
* Support selects that are connected with "OR" statements, not just "AND"
* Support different types, e.g. String GUIDs, for primary keys
* Add methods that allow for updates and deletes based on criteria other than primary key