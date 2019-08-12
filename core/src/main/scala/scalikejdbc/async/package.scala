package scalikejdbc

import scala.concurrent.Future
import scalikejdbc.async.ShortenedNames._
/**
 * Default package to import
 *
 * {{{
 *   import scalikejdbc._, async._
 * }}}
 */
package object async {

  // ---------------------
  // implicit conversions
  // ---------------------

  implicit def makeSQLExecutionAsync(sql: SQLExecution): AsyncSQLExecution = {
    new AsyncSQLExecutionImpl(sql)
  }

  implicit def makeSQLUpdateAsync(sql: SQLUpdate): AsyncSQLUpdate = {
    new AsyncSQLUpdateImpl(sql)
  }

  implicit def makeSQLUpdateAndReturnGeneratedKeyAsync(sql: SQLUpdateWithGeneratedKey): AsyncSQLUpdateAndReturnGeneratedKey = {
    new AsyncSQLUpdateAndReturnGeneratedKeyImpl(sql)
  }

  // --------------
  // one-to-x -> Option
  // --------------

  implicit def makeSQLToOptionAsync[A, B1, B2, B3, B4, B5, Z](sql: SQLToOption[A, HasExtractor]): AsyncSQLToOption[A] = {
    new AsyncSQLToOptionImpl[A](sql)
  }
  implicit def makeSQLToOptionAsync[A, B1, B2, B3, B4, B5, Z](sql: SQLToOptionImpl[A, HasExtractor]): AsyncSQLToOption[A] = {
    new AsyncSQLToOptionImpl[A](sql)
  }
  implicit def makeOneToOneSQLToOptionAsync[A, B, Z](sql: OneToOneSQLToOption[A, B, HasExtractor, Z]): AsyncOneToOneSQLToOption[A, B, Z] = {
    new AsyncOneToOneSQLToOption[A, B, Z](sql)
  }
  implicit def makeOneToManySQLToOptionAsync[A, B, Z](sql: OneToManySQLToOption[A, B, HasExtractor, Z]): AsyncOneToManySQLToOption[A, B, Z] = {
    new AsyncOneToManySQLToOption[A, B, Z](sql)
  }
  implicit def makeOneToManies2SQLToOptionAsync[A, B1, B2, Z](sql: OneToManies2SQLToOption[A, B1, B2, HasExtractor, Z]): AsyncOneToManies2SQLToOption[A, B1, B2, Z] = {
    new AsyncOneToManies2SQLToOption[A, B1, B2, Z](sql)
  }
  implicit def makeOneToManies3SQLToOptionAsync[A, B1, B2, B3, Z](sql: OneToManies3SQLToOption[A, B1, B2, B3, HasExtractor, Z]): AsyncOneToManies3SQLToOption[A, B1, B2, B3, Z] = {
    new AsyncOneToManies3SQLToOption[A, B1, B2, B3, Z](sql)
  }
  implicit def makeOneToManies4SQLToOptionAsync[A, B1, B2, B3, B4, Z](sql: OneToManies4SQLToOption[A, B1, B2, B3, B4, HasExtractor, Z]): AsyncOneToManies4SQLToOption[A, B1, B2, B3, B4, Z] = {
    new AsyncOneToManies4SQLToOption[A, B1, B2, B3, B4, Z](sql)
  }
  implicit def makeOneToManies5SQLToOptionAsync[A, B1, B2, B3, B4, B5, Z](sql: OneToManies5SQLToOption[A, B1, B2, B3, B4, B5, HasExtractor, Z]): AsyncOneToManies5SQLToOption[A, B1, B2, B3, B4, B5, Z] = {
    new AsyncOneToManies5SQLToOption[A, B1, B2, B3, B4, B5, Z](sql)
  }
  implicit def makeOneToManies6SQLToOptionAsync[A, B1, B2, B3, B4, B5, B6, Z](sql: OneToManies6SQLToOption[A, B1, B2, B3, B4, B5, B6, HasExtractor, Z]): AsyncOneToManies6SQLToOption[A, B1, B2, B3, B4, B5, B6, Z] = {
    new AsyncOneToManies6SQLToOption[A, B1, B2, B3, B4, B5, B6, Z](sql)
  }
  implicit def makeOneToManies7SQLToOptionAsync[A, B1, B2, B3, B4, B5, B6, B7, Z](sql: OneToManies7SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, HasExtractor, Z]): AsyncOneToManies7SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, Z] = {
    new AsyncOneToManies7SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, Z](sql)
  }
  implicit def makeOneToManies8SQLToOptionAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, Z](sql: OneToManies8SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, HasExtractor, Z]): AsyncOneToManies8SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, Z] = {
    new AsyncOneToManies8SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, Z](sql)
  }
  implicit def makeOneToManies9SQLToOptionAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z](sql: OneToManies9SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, HasExtractor, Z]): AsyncOneToManies9SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z] = {
    new AsyncOneToManies9SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z](sql)
  }
  implicit def makeOneToManies10SQLToOptionAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Z](sql: OneToManies10SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, HasExtractor, Z]): AsyncOneToManies10SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Z] = {
    new AsyncOneToManies10SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Z](sql)
  }
  implicit def makeOneToManies11SQLToOptionAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, Z](sql: OneToManies11SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, HasExtractor, Z]): AsyncOneToManies11SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, Z] = {
    new AsyncOneToManies11SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, Z](sql)
  }
  implicit def makeOneToManies12SQLToOptionAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, Z](sql: OneToManies12SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, HasExtractor, Z]): AsyncOneToManies12SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, Z] = {
    new AsyncOneToManies12SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, Z](sql)
  }
  implicit def makeOneToManies13SQLToOptionAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, Z](sql: OneToManies13SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, HasExtractor, Z]): AsyncOneToManies13SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, Z] = {
    new AsyncOneToManies13SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, Z](sql)
  }
  implicit def makeOneToManies14SQLToOptionAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, Z](sql: OneToManies14SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, HasExtractor, Z]): AsyncOneToManies14SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, Z] = {
    new AsyncOneToManies14SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, Z](sql)
  }
  implicit def makeOneToManies15SQLToOptionAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, Z](sql: OneToManies15SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, HasExtractor, Z]): AsyncOneToManies15SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, Z] = {
    new AsyncOneToManies15SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, Z](sql)
  }
  implicit def makeOneToManies16SQLToOptionAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, Z](sql: OneToManies16SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, HasExtractor, Z]): AsyncOneToManies16SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, Z] = {
    new AsyncOneToManies16SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, Z](sql)
  }
  implicit def makeOneToManies17SQLToOptionAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, Z](sql: OneToManies17SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, HasExtractor, Z]): AsyncOneToManies17SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, Z] = {
    new AsyncOneToManies17SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, Z](sql)
  }
  implicit def makeOneToManies18SQLToOptionAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, Z](sql: OneToManies18SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, HasExtractor, Z]): AsyncOneToManies18SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, Z] = {
    new AsyncOneToManies18SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, Z](sql)
  }
  implicit def makeOneToManies19SQLToOptionAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, Z](sql: OneToManies19SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, HasExtractor, Z]): AsyncOneToManies19SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, Z] = {
    new AsyncOneToManies19SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, Z](sql)
  }
  implicit def makeOneToManies20SQLToOptionAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, Z](sql: OneToManies20SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, HasExtractor, Z]): AsyncOneToManies20SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, Z] = {
    new AsyncOneToManies20SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, Z](sql)
  }
  implicit def makeOneToManies21SQLToOptionAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, Z](sql: OneToManies21SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, HasExtractor, Z]): AsyncOneToManies21SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, Z] = {
    new AsyncOneToManies21SQLToOption[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, Z](sql)
  }

  // --------------
  // one-to-x -> Iterable
  // --------------

  implicit def makeSQLToIterableAsync[A, B1, B2, B3, B4, B5, Z](sql: SQLToIterable[A, HasExtractor]): AsyncSQLToIterable[A] = {
    new AsyncSQLToIterableImpl[A](sql)
  }
  implicit def makeSQLToIterableAsync[A, B1, B2, B3, B4, B5, Z](sql: SQLToIterableImpl[A, HasExtractor]): AsyncSQLToIterable[A] = {
    new AsyncSQLToIterableImpl[A](sql)
  }
  implicit def makeOneToOneSQLToIterableAsync[A, B, Z](sql: OneToOneSQLToIterable[A, B, HasExtractor, Z]): AsyncOneToOneSQLToIterable[A, B, Z] = {
    new AsyncOneToOneSQLToIterable[A, B, Z](sql)
  }
  implicit def makeOneToManySQLToIterableAsync[A, B, Z](sql: OneToManySQLToIterable[A, B, HasExtractor, Z]): AsyncOneToManySQLToIterable[A, B, Z] = {
    new AsyncOneToManySQLToIterable[A, B, Z](sql)
  }
  implicit def makeOneToManies2SQLToIterableAsync[A, B1, B2, Z](sql: OneToManies2SQLToIterable[A, B1, B2, HasExtractor, Z]): AsyncOneToManies2SQLToIterable[A, B1, B2, Z] = {
    new AsyncOneToManies2SQLToIterable[A, B1, B2, Z](sql)
  }
  implicit def makeOneToManies3SQLToIterableAsync[A, B1, B2, B3, Z](sql: OneToManies3SQLToIterable[A, B1, B2, B3, HasExtractor, Z]): AsyncOneToManies3SQLToIterable[A, B1, B2, B3, Z] = {
    new AsyncOneToManies3SQLToIterable[A, B1, B2, B3, Z](sql)
  }
  implicit def makeOneToManies4SQLToIterableAsync[A, B1, B2, B3, B4, Z](sql: OneToManies4SQLToIterable[A, B1, B2, B3, B4, HasExtractor, Z]): AsyncOneToManies4SQLToIterable[A, B1, B2, B3, B4, Z] = {
    new AsyncOneToManies4SQLToIterable[A, B1, B2, B3, B4, Z](sql)
  }
  implicit def makeOneToManies5SQLToIterableAsync[A, B1, B2, B3, B4, B5, Z](sql: OneToManies5SQLToIterable[A, B1, B2, B3, B4, B5, HasExtractor, Z]): AsyncOneToManies5SQLToIterable[A, B1, B2, B3, B4, B5, Z] = {
    new AsyncOneToManies5SQLToIterable[A, B1, B2, B3, B4, B5, Z](sql)
  }
  implicit def makeOneToManies6SQLToIterableAsync[A, B1, B2, B3, B4, B5, B6, Z](sql: OneToManies6SQLToIterable[A, B1, B2, B3, B4, B5, B6, HasExtractor, Z]): AsyncOneToManies6SQLToIterable[A, B1, B2, B3, B4, B5, B6, Z] = {
    new AsyncOneToManies6SQLToIterable[A, B1, B2, B3, B4, B5, B6, Z](sql)
  }
  implicit def makeOneToManies7SQLToIterableAsync[A, B1, B2, B3, B4, B5, B6, B7, Z](sql: OneToManies7SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, HasExtractor, Z]): AsyncOneToManies7SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, Z] = {
    new AsyncOneToManies7SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, Z](sql)
  }
  implicit def makeOneToManies8SQLToIterableAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, Z](sql: OneToManies8SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, HasExtractor, Z]): AsyncOneToManies8SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, Z] = {
    new AsyncOneToManies8SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, Z](sql)
  }
  implicit def makeOneToManies9SQLToIterableAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z](sql: OneToManies9SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, HasExtractor, Z]): AsyncOneToManies9SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z] = {
    new AsyncOneToManies9SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z](sql)
  }
  implicit def makeOneToManies10SQLToIterableAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Z](sql: OneToManies10SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, HasExtractor, Z]): AsyncOneToManies10SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Z] = {
    new AsyncOneToManies10SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Z](sql)
  }
  implicit def makeOneToManies11SQLToIterableAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, Z](sql: OneToManies11SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, HasExtractor, Z]): AsyncOneToManies11SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, Z] = {
    new AsyncOneToManies11SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, Z](sql)
  }
  implicit def makeOneToManies12SQLToIterableAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, Z](sql: OneToManies12SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, HasExtractor, Z]): AsyncOneToManies12SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, Z] = {
    new AsyncOneToManies12SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, Z](sql)
  }
  implicit def makeOneToManies13SQLToIterableAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, Z](sql: OneToManies13SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, HasExtractor, Z]): AsyncOneToManies13SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, Z] = {
    new AsyncOneToManies13SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, Z](sql)
  }
  implicit def makeOneToManies14SQLToIterableAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, Z](sql: OneToManies14SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, HasExtractor, Z]): AsyncOneToManies14SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, Z] = {
    new AsyncOneToManies14SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, Z](sql)
  }
  implicit def makeOneToManies15SQLToIterableAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, Z](sql: OneToManies15SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, HasExtractor, Z]): AsyncOneToManies15SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, Z] = {
    new AsyncOneToManies15SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, Z](sql)
  }
  implicit def makeOneToManies16SQLToIterableAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, Z](sql: OneToManies16SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, HasExtractor, Z]): AsyncOneToManies16SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, Z] = {
    new AsyncOneToManies16SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, Z](sql)
  }
  implicit def makeOneToManies17SQLToIterableAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, Z](sql: OneToManies17SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, HasExtractor, Z]): AsyncOneToManies17SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, Z] = {
    new AsyncOneToManies17SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, Z](sql)
  }
  implicit def makeOneToManies18SQLToIterableAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, Z](sql: OneToManies18SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, HasExtractor, Z]): AsyncOneToManies18SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, Z] = {
    new AsyncOneToManies18SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, Z](sql)
  }
  implicit def makeOneToManies19SQLToIterableAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, Z](sql: OneToManies19SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, HasExtractor, Z]): AsyncOneToManies19SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, Z] = {
    new AsyncOneToManies19SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, Z](sql)
  }
  implicit def makeOneToManies20SQLToIterableAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, Z](sql: OneToManies20SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, HasExtractor, Z]): AsyncOneToManies20SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, Z] = {
    new AsyncOneToManies20SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, Z](sql)
  }
  implicit def makeOneToManies21SQLToIterableAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, Z](sql: OneToManies21SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, HasExtractor, Z]): AsyncOneToManies21SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, Z] = {
    new AsyncOneToManies21SQLToIterable[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, Z](sql)
  }

  // --------------
  // one-to-x -> List
  // --------------

  implicit def makeSQLToListAsync[A, B1, B2, B3, B4, B5, Z](sql: SQLToList[A, HasExtractor]): AsyncSQLToList[A] = {
    new AsyncSQLToListImpl[A](sql)
  }
  implicit def makeSQLToListAsync[A, B1, B2, B3, B4, B5, Z](sql: SQLToListImpl[A, HasExtractor]): AsyncSQLToList[A] = {
    new AsyncSQLToListImpl[A](sql)
  }
  implicit def makeOneToOneSQLToListAsync[A, B, Z](sql: OneToOneSQLToList[A, B, HasExtractor, Z]): AsyncOneToOneSQLToList[A, B, Z] = {
    new AsyncOneToOneSQLToList[A, B, Z](sql)
  }
  implicit def makeOneToManySQLToListAsync[A, B, Z](sql: OneToManySQLToList[A, B, HasExtractor, Z]): AsyncOneToManySQLToList[A, B, Z] = {
    new AsyncOneToManySQLToList[A, B, Z](sql)
  }
  implicit def makeOneToManies2SQLToListAsync[A, B1, B2, Z](sql: OneToManies2SQLToList[A, B1, B2, HasExtractor, Z]): AsyncOneToManies2SQLToList[A, B1, B2, Z] = {
    new AsyncOneToManies2SQLToList[A, B1, B2, Z](sql)
  }
  implicit def makeOneToManies3SQLToListAsync[A, B1, B2, B3, Z](sql: OneToManies3SQLToList[A, B1, B2, B3, HasExtractor, Z]): AsyncOneToManies3SQLToList[A, B1, B2, B3, Z] = {
    new AsyncOneToManies3SQLToList[A, B1, B2, B3, Z](sql)
  }
  implicit def makeOneToManies4SQLToListAsync[A, B1, B2, B3, B4, Z](sql: OneToManies4SQLToList[A, B1, B2, B3, B4, HasExtractor, Z]): AsyncOneToManies4SQLToList[A, B1, B2, B3, B4, Z] = {
    new AsyncOneToManies4SQLToList[A, B1, B2, B3, B4, Z](sql)
  }
  implicit def makeOneToManies5SQLToListAsync[A, B1, B2, B3, B4, B5, Z](sql: OneToManies5SQLToList[A, B1, B2, B3, B4, B5, HasExtractor, Z]): AsyncOneToManies5SQLToList[A, B1, B2, B3, B4, B5, Z] = {
    new AsyncOneToManies5SQLToList[A, B1, B2, B3, B4, B5, Z](sql)
  }
  implicit def makeOneToManies6SQLToListAsync[A, B1, B2, B3, B4, B5, B6, Z](sql: OneToManies6SQLToList[A, B1, B2, B3, B4, B5, B6, HasExtractor, Z]): AsyncOneToManies6SQLToList[A, B1, B2, B3, B4, B5, B6, Z] = {
    new AsyncOneToManies6SQLToList[A, B1, B2, B3, B4, B5, B6, Z](sql)
  }
  implicit def makeOneToManies7SQLToListAsync[A, B1, B2, B3, B4, B5, B6, B7, Z](sql: OneToManies7SQLToList[A, B1, B2, B3, B4, B5, B6, B7, HasExtractor, Z]): AsyncOneToManies7SQLToList[A, B1, B2, B3, B4, B5, B6, B7, Z] = {
    new AsyncOneToManies7SQLToList[A, B1, B2, B3, B4, B5, B6, B7, Z](sql)
  }
  implicit def makeOneToManies8SQLToListAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, Z](sql: OneToManies8SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, HasExtractor, Z]): AsyncOneToManies8SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, Z] = {
    new AsyncOneToManies8SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, Z](sql)
  }
  implicit def makeOneToManies9SQLToListAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z](sql: OneToManies9SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, HasExtractor, Z]): AsyncOneToManies9SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z] = {
    new AsyncOneToManies9SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, Z](sql)
  }
  implicit def makeOneToManies10SQLToListAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Z](sql: OneToManies10SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, HasExtractor, Z]): AsyncOneToManies10SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Z] = {
    new AsyncOneToManies10SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Z](sql)
  }
  implicit def makeOneToManies11SQLToListAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, Z](sql: OneToManies11SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, HasExtractor, Z]): AsyncOneToManies11SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, Z] = {
    new AsyncOneToManies11SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, Z](sql)
  }
  implicit def makeOneToManies12SQLToListAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, Z](sql: OneToManies12SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, HasExtractor, Z]): AsyncOneToManies12SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, Z] = {
    new AsyncOneToManies12SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, Z](sql)
  }
  implicit def makeOneToManies13SQLToListAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, Z](sql: OneToManies13SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, HasExtractor, Z]): AsyncOneToManies13SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, Z] = {
    new AsyncOneToManies13SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, Z](sql)
  }
  implicit def makeOneToManies14SQLToListAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, Z](sql: OneToManies14SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, HasExtractor, Z]): AsyncOneToManies14SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, Z] = {
    new AsyncOneToManies14SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, Z](sql)
  }
  implicit def makeOneToManies15SQLToListAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, Z](sql: OneToManies15SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, HasExtractor, Z]): AsyncOneToManies15SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, Z] = {
    new AsyncOneToManies15SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, Z](sql)
  }
  implicit def makeOneToManies16SQLToListAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, Z](sql: OneToManies16SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, HasExtractor, Z]): AsyncOneToManies16SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, Z] = {
    new AsyncOneToManies16SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, Z](sql)
  }
  implicit def makeOneToManies17SQLToListAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, Z](sql: OneToManies17SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, HasExtractor, Z]): AsyncOneToManies17SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, Z] = {
    new AsyncOneToManies17SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, Z](sql)
  }
  implicit def makeOneToManies18SQLToListAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, Z](sql: OneToManies18SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, HasExtractor, Z]): AsyncOneToManies18SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, Z] = {
    new AsyncOneToManies18SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, Z](sql)
  }
  implicit def makeOneToManies19SQLToListAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, Z](sql: OneToManies19SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, HasExtractor, Z]): AsyncOneToManies19SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, Z] = {
    new AsyncOneToManies19SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, Z](sql)
  }
  implicit def makeOneToManies20SQLToListAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, Z](sql: OneToManies20SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, HasExtractor, Z]): AsyncOneToManies20SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, Z] = {
    new AsyncOneToManies20SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, Z](sql)
  }
  implicit def makeOneToManies21SQLToListAsync[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, Z](sql: OneToManies21SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, HasExtractor, Z]): AsyncOneToManies21SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, Z] = {
    new AsyncOneToManies21SQLToList[A, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, Z](sql)
  }

  // ---------------------
  // utilities
  // ---------------------

  object updateFuture {
    def apply(sql: => SQLBuilder[_])(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Int] = {
      withSQL(sql).update.future
    }
  }

  object executeFuture {
    def apply(sql: => SQLBuilder[_])(implicit session: AsyncDBSession, cxt: EC = ECGlobal): Future[Boolean] = {
      withSQL(sql).execute.future
    }
  }

}
