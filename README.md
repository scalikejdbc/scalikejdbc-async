## ScalikeJDBC-Async

### ScalikeJDBC Extension: Non-blocking APIs in the JDBC way

ScalikeJDBC-Async provides non-blocking APIs to talk with PostgreSQL and MySQL in the JDBC way. 

This library is built with postgrsql-async and mysql-async, incredible works by @mauricio.

ScalikeJDBC:

https://github.com/seratch/scalikejdbc

ScalikeJDBC is a tidy SQL-based DB access library for Scala developers. This library naturally wraps JDBC APIs and provides you easy-to-use APIs.


### Supported RDBMS

- PostgreSQL
- MySQL

### Dependencies

Add `scalikejdbc-async` to your dependencies.

```scala
libraryDependencies ++= Seq(
  "com.github.seratch"  %% "scalikejdbc-async" % "0.2.0",
  "com.github.mauricio" %% "postgresql-async"  % "0.2.4",
  "com.github.mauricio" %% "mysql-async"       % "0.2.4",
  "org.slf4j"           %  "slf4j-simple"      % "[1.7,)" // slf4j implementation
)
```

### Example

[programmerlist/ExampleSpec.scala](https://github.com/seratch/scalikejdbc-async/blob/master/src/test/scala/programmerlist/ExampleSpec.scala)

```scala
// create a new record within a transaction
val created: Future[Company] = AsyncDB.localTx { implicit tx =>
  for {
    company <- Company.create("ScalikeJDBC, Inc.", Some("http://scalikejdbc.org/"))
    seratch <- Programmer.create("seratch", Some(company.id))
    gakuzzzz <- Programmer.create("gakuzzzz", Some(company.id))
    tototoshi <- Programmer.create("tototoshi", Some(company.id))
    cb372 <- Programmer.create("cb372", Some(company.id))
  } yield company
}

Await.result(created, 5.seconds)

created.foreach { newCompany: Company =>

  // delete a record and rollback
  val withinTx: Future[Unit] = AsyncDB.localTx { implicit tx =>
    for {
      restructuring <- Programmer.findAllBy(sqls.eq(p.companyId, newCompany.id)).map { 
        programmers => programmers.foreach(_.destroy()) 
      }
      dissolution <- newCompany.destroy()
      failure <- sql"Just joking!".update.future
    } yield ()
  }

  try Await.result(withinTx, 5.seconds)
  catch { case e: Exception => log.debug(e.getMessage, e) }

  // rollback expected
  val company = AsyncDB.withPool { implicit s =>
    Company.find(newCompany.id)
  }
  Await.result(company, 5.seconds)
  company.foreach { c => c.isDefined should be(true) }
}
```

### FAQ

#### Is it possible to combine scalikejdbc-async with normal scalikejdbc?

Yes, it's possible. See this example spec:

[sample/PostgreSQLSampleSpec.scala](https://github.com/seratch/scalikejdbc-async/blob/master/src/test/scala/sample/PostgreSQLSampleSpec.scala)

#### Why isn't it a part of scalikejdbc project now?

This library is still in alpha stage. If this library becomes stable enough, it will be merged into the ScalikeJDBC project.


### License

Published binary files have the following copyright:

```
Copyright 2013 ScalikeJDBC committers

Apache License, Version 2.0

http://www.apache.org/licenses/LICENSE-2.0.html
```

