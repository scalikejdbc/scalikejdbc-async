## scalikejdbc-async

ScalikeJDBC Async provides non-blocking APIs to talk with RDBMS. This library is built with postgrsql-async and mysql-async by @mauricio.

### Supported RDBMS

We never release without passing all the unit tests with the following RDBMS.

- PostgreSQL
- MySQL


### Usage

Add `scalikejdbc-async` to your dependencies.

```scala
libraryDependencies ++= Seq(
  "com.github.seratch"  %% "scalikejdbc-async" % "0.2.0",
  "com.github.mauricio" %% "postgresql-async"  % "0.2.4",
  "com.github.mauricio" %% "mysql-async"       % "0.2.4",
  "org.slf4j"           %  "slf4j-simple"      % "[1.7,)" // slf4j implementation
)
```

Usage is pretty simple, just call `#future()` instead of `#apply()`.

```scala
import scalikejdbc._, async._
AsyncConnectionPool.singleton(jdbcUrl, user, password)

val company: Future[Option[Company]] = AsyncDB.withPool { implicit s =>
  withSQL { 
    select.from(Company as c).where.eq(c.id, 123) 
  }.map(Company(c)).single.future()
}
```

Transactional operations are also supported. 

```scala
import scalikejdbc._, async._, FutureImplicits._
val wc = Worker.column

val companyId: Future[Long] = AsyncDB.localTx { implicit tx =>
  for {
    companyId <- withSQL {
        insert.into(Company).values("Typesafe", DateTime.now)
      }.updateAndReturnGeneratedKey
     _ <- update(Worker).set(wc.companyId -> companyId).where.eq(wc.id, 123)
  } yield companyId
}
```

### TODO

- ConnectionPool -> Done
- ConnectionPoolFactory -> Heroku
- Transaction control and returned values -> Done
- implicit ExecutionContext -> Done
- updateAndReturnGeneratedKey API -> Done
- AsyncTx chain -> Done
- Logging -> Done
- oneToX API
- More examples

### License

Published binary files have the following copyright:

```
Copyright 2013 ScalikeJDBC committers

Apache License, Version 2.0

http://www.apache.org/licenses/LICENSE-2.0.html
```

