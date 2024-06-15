case class GeneratedFile(name: String, code: String)

object CodeGenerator {

  val AsyncDBSessionBoilerplateHeader = """package scalikejdbc.async

import scala.concurrent.{Future, ExecutionContext}
import scala.collection.mutable.LinkedHashMap
import scalikejdbc.async.ShortenedNames.ECGlobal
import scalikejdbc.WrappedResultSet

abstract class AsyncDBSessionBoilerplate { self: AsyncDBSession =>
"""

  val PackageBoilerplateHeader = """package scalikejdbc
package async

abstract class PackageBoilerplate {
"""

  val generate: Seq[GeneratedFile] = {
    val max = 21
    Seq(
      (2 to max).flatMap { n =>
        Seq(
          GeneratedFile(
            s"AsyncOneToManies${n}SQLToOption.scala",
            AsyncOneToManiesSQLToOption(n)
          ),
          GeneratedFile(
            s"AsyncOneToManies${n}SQLToIterable.scala",
            AsyncOneToManiesSQLToIterable(n)
          ),
          GeneratedFile(
            s"AsyncOneToManies${n}SQLToList.scala",
            AsyncOneToManiesSQLToList(n)
          )
        )
      }, {
        val methods = (2 to max)
          .flatMap { n =>
            oneToManiesIterable(n) +: {
              if n <= 9 then {
                Seq(oneToManiesTraversable(n))
              } else {
                Nil
              }
            }
          }
          .mkString(AsyncDBSessionBoilerplateHeader, "\n", "}\n")
        Seq(
          GeneratedFile("AsyncDBSessionBoilerplate.scala", methods)
        )
      }, {
        val methods = (2 to max)
          .flatMap { n =>
            Seq(
              makeOneToManiesSQLToIterableAsync(n),
              makeOneToManiesSQLToListAsync(n),
              makeOneToManiesSQLToOptionAsync(n)
            )
          }
          .mkString(PackageBoilerplateHeader, "\n", "}\n")
        Seq(
          GeneratedFile("PackageBoilerplate.scala", methods)
        )
      }
    ).flatten
  }

  def B1_to_BN(n: Int) = (1 to n).map("B" + _).mkString(", ")
  def extractTo(n: Int) =
    (1 to n).map("underlying.extractTo" + _).mkString(", ")
  def seqB(n: Int) = (1 to n).map(i => s"Seq[B${i}]").mkString(", ")

  def AsyncOneToManiesSQLToOption(n: Int): String = {
    s"""package scalikejdbc.async

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scalikejdbc.async.ShortenedNames.ECGlobal
import scalikejdbc.{OneToManies${n}SQLToOption, HasExtractor, TooManyRowsException}

class AsyncOneToManies${n}SQLToOption[A, ${B1_to_BN(
        n
      )}, Z](val underlying: OneToManies${n}SQLToOption[A, ${B1_to_BN(
        n
      )}, HasExtractor, Z])
  extends AnyVal
    with AsyncSQLToOption[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: ExecutionContext = ECGlobal): Future[Option[Z]] = {
    session.oneToManies${n}Iterable(underlying.statement, underlying.rawParameters.toSeq*)(underlying.extractOne)(
      ${extractTo(n)}
    )(underlying.transform).map {
      case Nil => None
      case results if results.size == 1 => results.headOption
      case results => throw new TooManyRowsException(1, results.size)
    }
  }
}
"""
  }

  def AsyncOneToManiesSQLToIterable(n: Int): String = {
    s"""package scalikejdbc.async

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scalikejdbc.async.ShortenedNames.ECGlobal
import scalikejdbc.{OneToManies${n}SQLToIterable, HasExtractor}

class AsyncOneToManies${n}SQLToIterable[A, ${B1_to_BN(
        n
      )}, Z](val underlying: OneToManies${n}SQLToIterable[A, ${B1_to_BN(
        n
      )}, HasExtractor, Z])
  extends AnyVal
    with AsyncSQLToIterable[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: ExecutionContext = ECGlobal): Future[Iterable[Z]] = {
    session.oneToManies${n}Iterable(underlying.statement, underlying.rawParameters.toSeq*)(underlying.extractOne)(
      ${extractTo(n)}
    )(underlying.transform)
  }
}
"""
  }

  def AsyncOneToManiesSQLToList(n: Int): String = {
    s"""package scalikejdbc.async

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scalikejdbc.async.ShortenedNames.ECGlobal
import scalikejdbc.{OneToManies${n}SQLToList, HasExtractor}

class AsyncOneToManies${n}SQLToList[A, ${B1_to_BN(
        n
      )}, Z](val underlying: OneToManies${n}SQLToList[A, ${B1_to_BN(
        n
      )}, HasExtractor, Z])
  extends AnyVal
    with AsyncSQLToList[Z] {
  override def future()(implicit session: AsyncDBSession, cxt: ExecutionContext = ECGlobal): Future[List[Z]] = {
    val iterable = session.oneToManies${n}Iterable(underlying.statement, underlying.rawParameters.toSeq*)(underlying.extractOne)(
      ${extractTo(n)}
    )(underlying.transform)
    iterable.map(_.toList)
  }
}
"""
  }

  def oneToManiesTraversable(n: Int): String = {
    s"""
  @deprecated("will be removed. use oneToManies${n}Iterable", "0.12.0")
  final def oneToManies${n}Traversable[A, ${B1_to_BN(n)}, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    ${(1 to n)
        .map(i => s"extractTo${i}: WrappedResultSet => Option[B${i}]")
        .mkString(", ")}
  )(
    transform: (A, ${seqB(n)}) => Z
  )(
    implicit
    cxt: ExecutionContext = ECGlobal
  ): Future[Iterable[Z]] = {
    oneToManies${n}Iterable[A, ${B1_to_BN(
        n
      )}, Z](statement, parameters*)(extractOne)(
      ${(1 to n).map("extractTo" + _).mkString(", ")}
    )(transform)
  }"""
  }

  def oneToManiesIterable(n: Int): String = {
    s"""
  def oneToManies${n}Iterable[A, ${B1_to_BN(n)}, Z](
    statement: String,
    parameters: Any*)(
    extractOne: WrappedResultSet => A)(
    ${(1 to n)
        .map(i => s"extractTo${i}: WrappedResultSet => Option[B${i}]")
        .mkString(", ")}
  )(
    transform: (A, ${seqB(n)}) => Z
  )(
    implicit
    cxt: ExecutionContext = ECGlobal
  ): Future[Iterable[Z]] = {
    val _parameters = ensureAndNormalizeParameters(parameters)
    withListeners(statement, _parameters) {
      queryLogging(statement, _parameters)

      def processResultSet(result: LinkedHashMap[A, (${seqB(
        n
      )})], rs: WrappedResultSet): LinkedHashMap[A, (${seqB(n)})] = {
        val o = extractOne(rs)
        ${(1 to n).map(i => s"val to${i} = extractTo${i}(rs)").mkString("; ")}
        result.keys.find(_ == o).map { _ =>
          (${(1 to n).map("to" + _).mkString(" orElse ")}).map { _ =>
            val (${(1 to n).map("ts" + _).mkString(", ")}) = result.apply(o)
            result += (o -> (
              ${(1 to n)
        .map(i =>
          s"to${i}.map(t => if (ts${i}.contains(t)) ts${i} else ts${i} :+ t).getOrElse(ts${i})"
        )
        .mkString(", ")}
            ))
          }.getOrElse(result)
        }.getOrElse {
          result += (
            o -> (
              ${(1 to n)
        .map(i => s"to${i}.map(Vector(_)).getOrElse(Vector.empty)")
        .mkString(", ")}
            )
          )
        }
      }

      connection.sendPreparedStatement(statement, _parameters*).map { result =>
        result.rows.map { ars =>
          new AsyncResultSetIterator(ars).foldLeft(
            LinkedHashMap[A, (${seqB(n)})]())(processResultSet).map {
            case (one, (${(1 to n)
        .map("t" + _)
        .mkString(", ")})) => transform(one, ${(1 to n)
        .map("t" + _)
        .mkString(", ")})
          }
        }.getOrElse(Nil)
      }
    }
  }"""
  }

  def makeOneToManiesSQLToOptionAsync(n: Int): String = {
    s"""
  final implicit def makeOneToManies${n}SQLToOptionAsync[A, ${B1_to_BN(
        n
      )}, Z](sql: OneToManies${n}SQLToOption[A, ${B1_to_BN(
        n
      )}, HasExtractor, Z]): AsyncOneToManies${n}SQLToOption[A, ${B1_to_BN(
        n
      )}, Z] =
    new AsyncOneToManies${n}SQLToOption[A, ${B1_to_BN(n)}, Z](sql)"""
  }

  def makeOneToManiesSQLToIterableAsync(n: Int): String = {
    s"""
  final implicit def makeOneToManies${n}SQLToIterableAsync[A, ${B1_to_BN(
        n
      )}, Z](sql: OneToManies${n}SQLToIterable[A, ${B1_to_BN(
        n
      )}, HasExtractor, Z]): AsyncOneToManies${n}SQLToIterable[A, ${B1_to_BN(
        n
      )}, Z] =
    new AsyncOneToManies${n}SQLToIterable[A, ${B1_to_BN(n)}, Z](sql)"""
  }

  def makeOneToManiesSQLToListAsync(n: Int): String = {
    s"""
  final implicit def makeOneToManies${n}SQLToListAsync[A, ${B1_to_BN(
        n
      )}, Z](sql: OneToManies${n}SQLToList[A, ${B1_to_BN(
        n
      )}, HasExtractor, Z]): AsyncOneToManies${n}SQLToList[A, ${B1_to_BN(
        n
      )}, Z] =
    new AsyncOneToManies${n}SQLToList[A, ${B1_to_BN(n)}, Z](sql)"""
  }
}
