## ScalikeJDBC-Async

### ScalikeJDBC Extension: Non-blocking APIs in the JDBC way

ScalikeJDBC-Async provides non-blocking APIs to talk with PostgreSQL and MySQL in the JDBC way. 

This library is built with [postgrsql-async and mysql-async,incredible works by @mauricio](https://github.com/mauricio/postgresql-async).

![ScalikeJDBC Logo](http://scalikejdbc.org/img/logo.png)

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
  "com.github.seratch"  %% "scalikejdbc-async" % "[0.2,)",
  "com.github.mauricio" %% "postgresql-async"  % "[0.2,)",
  "com.github.mauricio" %% "mysql-async"       % "[0.2,)",
  "org.slf4j"           %  "slf4j-simple"      % "[1.7,)" // slf4j implementation
)
```

If you're a Play2 user, use play-plugin too!

```scala
val appDependencies = Seq(
  "com.github.seratch"  %% "scalikejdbc-async"             % "[0.2,)",
  "com.github.seratch"  %% "scalikejdbc-async-play-plugin" % "[0.2,)",
  "com.github.mauricio" %% "postgresql-async"              % "[0.2,)"
)
```

### Example

- [programmerlist/ExampleSpec.scala](https://github.com/seratch/scalikejdbc-async/blob/master/src/test/scala/programmerlist/ExampleSpec.scala)
- [programmerlist/Company.scala](https://github.com/seratch/scalikejdbc-async/blob/master/src/test/scala/programmerlist/Company.scala)
- [programmerlist/Programmer.scala](https://github.com/seratch/scalikejdbc-async/blob/master/src/test/scala/programmerlist/Programmer.scala)

```scala
import scalikejdbc._, SQLInterpolation._, async._
import scala.concurrent._, duration._, ExecutionContext.Implicits.global

// set up connection pool (that's all you need to do)
AsyncConnectionPool.singleton("jdbc:postgresql://localhost:5432/scalikejdbc", "sa", "sa")

// create a new record within a transaction
val created: Future[Company] = AsyncDB.localTx { implicit tx =>
  for {
    company <- Company.create("ScalikeJDBC, Inc.", Some("http://scalikejdbc.org/"))
    seratch <- Programmer.create("seratch", Some(company.id))
    gakuzzzz <- Programmer.create("gakuzzzz", Some(company.id))
    xuwei_k <- Programmer.create("xuwei-k", Some(company.id))
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
  val found: Option[Company]= company.value.get.get
  found.isDefined should be(true)
}
```

Transactional queries should be executed in series. You cannot use `Future.traverse` or `Future.sequence`.

#### Play2 Example

See the play-sample project:

https://github.com/seratch/scalikejdbc-async/blob/develop/play-sample

### FAQ

#### Is it possible to combine scalikejdbc-async with normal scalikejdbc?

Yes, it's possible. See this example spec:

[sample/PostgreSQLSampleSpec.scala](https://github.com/seratch/scalikejdbc-async/blob/master/src/test/scala/sample/PostgreSQLSampleSpec.scala)

#### Why isn't it a part of scalikejdbc project now?

This library is still in alpha stage. If this library becomes stable enough, it will be merged into the ScalikeJDBC project.

#### How to contribute?

Before sending pull requests, please prepare the following DB settings and write tests to ensure that your fixes work as expected. 

- PostgreSQL

```
url: jdbc:postgresql://localhost:5432/scalikejdbc
username: sa
password: sa
```

```
url: jdbc:postgresql://localhost:5432/scalikejdbc2
username: sa
password: sa
```

- MySQL

```
url: jdbc:mysql://localhost:3306/scalikejdbc
username: sa
password: sa
```

### License

Published binary files have the following copyright:

```
Copyright 2013 ScalikeJDBC committers

Apache License, Version 2.0

http://www.apache.org/licenses/LICENSE-2.0.html
```

