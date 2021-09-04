package scalikejdbc.async.internal

import java.time.ZoneOffset

private[internal] object DateConvert {
  def unapply[A](a: A): Option[java.util.Date] = PartialFunction.condOpt(a) {
    case x: java.util.Date =>
      x
    case x: org.joda.time.ReadableInstant =>
      new java.util.Date(x.getMillis)
    case x: org.joda.time.LocalDateTime =>
      new java.util.Date(x.toDateTime.getMillis)
    case x: org.joda.time.LocalDate =>
      x.toDate
    case x: org.joda.time.LocalTime =>
      new java.util.Date(x.toDateTimeToday.getMillis)
    case x: scala.concurrent.duration.FiniteDuration =>
      new java.util.Date(x.toMillis)
    case x: java.time.Instant =>
      java.sql.Timestamp.from(x)
    case x: java.time.LocalDateTime =>
      java.sql.Timestamp.from(x.toInstant(ZoneOffset.UTC))
    case x: java.time.OffsetDateTime =>
      java.sql.Timestamp.from(x.toInstant)
    case x: java.time.LocalDate =>
      java.sql.Timestamp.from(x.atStartOfDay.toInstant(ZoneOffset.UTC))
    case x: java.time.LocalTime =>
      java.sql.Time.valueOf(x)
    case x: java.time.OffsetTime =>
      java.sql.Time.valueOf(x.toLocalTime)
  }
}
