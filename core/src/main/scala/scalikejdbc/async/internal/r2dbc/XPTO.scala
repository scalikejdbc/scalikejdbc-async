package scalikejdbc.async.internal.r2dbc

import io.r2dbc.spi.Row

import scala.jdk.CollectionConverters._

class XPTO(r: IndexedSeq[(String, AnyRef)]) {
  def get(index: Int): Any = r.lift(index).map(_._2).orNull
  def get(column: String): Any = r.collectFirst {
    case (n, o) if n == column => o
  }.orNull
}

object XPTO {
  def apply(row: Row): XPTO = {
    val x = row.getMetadata.getColumnMetadatas.asScala.map { m =>
      m.getName -> row.get(m.getName)
    }.toIndexedSeq
    new XPTO(x)
  }
}
