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
  "com.github.seratch" %% "scalikejdbc-async" % "0.2.0-SNAPSHOT",
  "com.github.seratch" %% "scalikejdbc-interpolation" % "[1.6,)", // optional
  "com.github.mauricio" %% "postgresql-async" % "0.2.4",
  //"com.github.mauricio" %% "mysql-async" % "0.2.4",
  "org.slf4j" % "slf4j-simple" % "[1.7,)" // slf4j implementation
)
```

Usage is pretty simple, just call `#future()` instead of `#apply()`.

```scala
import scalikejdbc._, async._

val (jdbcUrl, user, password) = ("jdbc:postgresql://localhost:5432/scalikejdbc", "sa", "sa")
AsyncConnectionPool.singleton(jdbcUrl, user, password)

val company: Future[Option[Company]] = AsyncDB.withPool { implicit session =>
  withSQL { 
    select.from(Company as c).where.eq(c.id, 123) 
  }.map(Company(c)).single.future()
}
```

Transactional operations are also supported. 

If you want to do more complex operations (e.g. after inserting new record, use the generated key), use blocking API.

```scala
val (m, tr) = (Member.column, TemporaryRegistraion.column)

val future: Future[Unit] = AsyncDB.withPool { implicit session =>
  AsyncTx.withBuidlers {
    insert.into(Member).namedValues(m.id -> 123, m.name -> "Bob"),
    delete.from(TemporaryRegisration).where.eq(tr.id, 123)
  }.future()
  // or AsyncTx.withSQLs { ... }.future()
}
```

### TODO

- ConnectionPool -> Done
- ConnectionPoolFactory -> Heroku
- Transaction control and returned values -> Done?
- implicit ExecutionContext -> Done
- Logging 
- Examples

### License

the Apache License, Version 2.0

```
/*
 * Copyright 2013 Kazuhiro Sera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
```

