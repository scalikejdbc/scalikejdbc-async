## ScalikeJDBC-Async [![Build Status](https://travis-ci.org/scalikejdbc/scalikejdbc-async.svg?branch=master)](https://travis-ci.org/scalikejdbc/scalikejdbc-async)

### ScalikeJDBC Extension: Non-blocking APIs in the JDBC way

ScalikeJDBC-Async provides non-blocking APIs to talk with PostgreSQL and MySQL in the JDBC way. 

This library is built with [jasync-sql](https://github.com/jasync-sql/jasync-sql).

![ScalikeJDBC Logo](http://scalikejdbc.org/img/logo.png)

ScalikeJDBC:

https://github.com/scalikejdbc/scalikejdbc

ScalikeJDBC is a tidy SQL-based DB access library for Scala developers. This library naturally wraps JDBC APIs and provides you easy-to-use APIs.


### Important Notice

ScalikeJDBC-Async is still in the beta stage. If you don't have motivation to investigate or fix issues by yourself, we recommend you waiting until stable version release someday.

### Supported RDBMS

- PostgreSQL
- MySQL

### Dependencies

Add `scalikejdbc-async` to your dependencies.

```scala
libraryDependencies ++= Seq(
  "org.scalikejdbc"       %% "scalikejdbc-async" % "0.12.+",
  "com.github.jasync-sql" %  "jasync-postgresql" % "1.0.+",
  "com.github.jasync-sql" %  "jasync-mysql"      % "1.0.+",
  "org.slf4j"             %  "slf4j-simple"      % "1.7.+" // slf4j implementation
)
```

### Example

- [programmerlist/ExampleSpec.scala](https://github.com/scalikejdbc/scalikejdbc-async/blob/master/core/src/test/scala/programmerlist/ExampleSpec.scala)
- [programmerlist/Company.scala](https://github.com/scalikejdbc/scalikejdbc-async/blob/master/core/src/test/scala/programmerlist/Company.scala)
- [programmerlist/Programmer.scala](https://github.com/scalikejdbc/scalikejdbc-async/blob/master/core/src/test/scala/programmerlist/Programmer.scala)

```scala
import scalikejdbc._, async._
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

### FAQ

#### Is it production-ready?

ScalikeJDBC-Async and [jasync-sql](https://github.com/jasync-sql/jasync-sql) basically works fine. However, to be honest, ScalikeJBDC-Async doesn't have much of a record of production applications.

#### Is it possible to combine scalikejdbc-async with normal scalikejdbc?

Yes, it's possible. See [this example](https://github.com/scalikejdbc/scalikejdbc-async/blob/master/core/src/test/scala/sample/PostgreSQLSampleSpec.scala).

#### Why isn't it a part of scalikejdbc project now?

This library is still in alpha stage. If this library becomes stable enough, it will be merged into the ScalikeJDBC project.

#### How to contribute?

Before sending pull requests, please install docker and run `sbt +test`

### License

Published binary files have the following copyright:

```
Copyright scalikejdbc.org

Apache License, Version 2.0

http://www.apache.org/licenses/LICENSE-2.0.html
```

