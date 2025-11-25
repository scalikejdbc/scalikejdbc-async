lazy val _version = "0.20.1-SNAPSHOT"
lazy val scalikejdbcVersion = "4.3.5"
lazy val jasyncVersion = "2.2.4" // provided
lazy val postgresqlVersion = "42.7.8"
val Scala212 = "2.12.20"
val Scala213 = "2.13.18"
val Scala3 = "3.3.7"

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

val mysqlConnectorJ = "com.mysql" % "mysql-connector-j" % "9.5.0" % "test"

lazy val core = (project in file("core")).settings(
  organization := "org.scalikejdbc",
  name := "scalikejdbc-async",
  version := _version,
  scalaVersion := Scala212,
  crossScalaVersions := Seq(Scala213, Scala212, Scala3),
  publishTo := (if (isSnapshot.value) None else localStaging.value),
  publishMavenStyle := true,
  // avoid NoClassDefFoundError
  // https://github.com/testcontainers/testcontainers-java/blob/22030eace3f4bafc735ccb0e402c1202329a95d1/core/src/main/java/org/testcontainers/utility/MountableFile.java#L284
  // https://github.com/sbt/sbt/issues/4794
  Test / fork := true,
  Test / javaOptions += "-Duser.timezone=GMT",
  Test / javaOptions += s"-Dmysql_version=${mysqlConnectorJ.revision}",
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
      "org.testcontainers" % "testcontainers-mysql" % "2.0.2" % "test",
      "org.testcontainers" % "testcontainers-postgresql" % "2.0.2" % "test",
      "org.postgresql" % "postgresql" % postgresqlVersion % "test",
      mysqlConnectorJ,
    )
  },
  libraryDependencies ++= {
    if (scala.util.Properties.isJavaAtLeast("11")) {
      Seq("ch.qos.logback" % "logback-classic" % "1.5.21" % "test")
    } else {
      Nil
    }
  },
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest-flatspec" % "3.2.19" % "test",
    "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.19" % "test",
  ),
  sbtPlugin := false,
  Global / transitiveClassifiers := Seq(Artifact.SourceClassifier),
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) =>
        Seq(
          "-Wconf:msg=package object inheritance:warning", // TODO
          "-Xsource:3",
        )
      case _ =>
        Nil
    }
  },
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

val _pomExtra = <url>https://scalikejdbc.org/</url>
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
