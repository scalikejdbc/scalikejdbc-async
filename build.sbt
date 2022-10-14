lazy val _version = "0.15.1-SNAPSHOT"
lazy val scalikejdbcVersion = "4.0.0"
lazy val jasyncVersion = "2.1.7" // provided
lazy val postgresqlVersion = "42.5.0"
val Scala212 = "2.12.17"
val Scala213 = "2.13.9"
val Scala3 = "3.2.0"

crossScalaVersions := Seq(Scala213, Scala212, Scala3)

lazy val unusedWarnings = Def.setting(
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, _)) =>
      Seq(
        "-Ywarn-unused:imports"
      )
    case _ =>
      Nil
  }
)

lazy val core = (project in file("core")).settings(
  organization := "org.scalikejdbc",
  name := "scalikejdbc-async",
  version := _version,
  scalaVersion := Scala212,
  crossScalaVersions := Seq(Scala213, Scala212, Scala3),
  publishTo := _publishTo(version.value),
  publishMavenStyle := true,
  // avoid NoClassDefFoundError
  // https://github.com/testcontainers/testcontainers-java/blob/22030eace3f4bafc735ccb0e402c1202329a95d1/core/src/main/java/org/testcontainers/utility/MountableFile.java#L284
  // https://github.com/sbt/sbt/issues/4794
  Test / fork := true,
  (Compile / packageSrc / mappings) ++= (Compile / managedSources).value.map {
    f =>
      // to merge generated sources into sources.jar as well
      (f, f.relativeTo((Compile / sourceManaged).value).get.getPath)
  },
  (Compile / sourceGenerators) += task {
    val dir = (Compile / sourceManaged).value / "scalikejdbc" / "async"
    CodeGenerator.generate.map { s =>
      val f = dir / s.name
      IO.write(f, s.code)
      f
    }
  },
  libraryDependencies ++= {
    Seq(
      "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2",
      "org.scalikejdbc" %% "scalikejdbc" % scalikejdbcVersion % "compile",
      "org.scalikejdbc" %% "scalikejdbc-interpolation" % scalikejdbcVersion % "compile",
      "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % scalikejdbcVersion % "test",
      "org.scalikejdbc" %% "scalikejdbc-joda-time" % scalikejdbcVersion % "test",
      "com.github.jasync-sql" % "jasync-postgresql" % jasyncVersion % "provided",
      "com.github.jasync-sql" % "jasync-mysql" % jasyncVersion % "provided",
      "com.dimafeng" %% "testcontainers-scala" % "0.40.11" % "test",
      "org.testcontainers" % "mysql" % "1.17.5" % "test",
      "org.testcontainers" % "postgresql" % "1.17.5" % "test",
      "org.postgresql" % "postgresql" % postgresqlVersion % "test",
      "mysql" % "mysql-connector-java" % "5.1.+" % "test",
      "ch.qos.logback" % "logback-classic" % "1.2.+" % "test"
    )
  },
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.2.14" % "test",
  ),
  sbtPlugin := false,
  Global / transitiveClassifiers := Seq(Artifact.SourceClassifier),
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-language:implicitConversions",
    "-feature"
  ) ++ unusedWarnings.value,
  Seq(Compile, Test).flatMap(c =>
    (c / console / scalacOptions) --= unusedWarnings.value
  ),
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 12 =>
        Seq("-Xfuture")
      case _ =>
        Nil
    }
  },
  publishMavenStyle := true,
  Test / publishArtifact := false,
  Test / parallelExecution := false,
  pomIncludeRepository := { x => false },
  pomExtra := _pomExtra
)

def _publishTo(v: String) = {
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
val _pomExtra = <url>http://scalikejdbc.org/</url>
      <licenses>
        <license>
          <name>Apache License, Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:scalikejdbc/scalikejdbc-async.git</url>
        <connection>scm:git:git@github.com:scalikejdbc/scalikejdbc-async.git</connection>
      </scm>
      <developers>
        <developer>
          <id>seratch</id>
          <name>Kazuhiro Sera</name>
          <url>https://github.com/seratch</url>
        </developer>
      </developers>
