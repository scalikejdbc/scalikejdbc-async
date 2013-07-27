import sbt._
import Keys._

object ScalikeJDBCAsyncProject extends Build {

  val scalikejdbcVersion = "1.6.6"
  val mauricioVersion = "0.2.4"

  lazy val scalikejdbcAsync = Project(
    id = "async",
    base = file("."),
    settings = Defaults.defaultSettings ++ Seq(
      organization := "com.github.seratch",
      name := "scalikejdbc-async",
      version := "0.2.0",
      scalaVersion := "2.10.2",
      publishTo <<= version { (v: String) => _publishTo(v) },
      publishMavenStyle := true,
      resolvers ++= _resolvers,
      libraryDependencies <++= (scalaVersion) { scalaVersion =>
        Seq(
          "com.github.seratch"  %% "scalikejdbc"               % scalikejdbcVersion % "compile",
          "com.github.seratch"  %% "scalikejdbc-interpolation" % scalikejdbcVersion % "compile",
          "com.github.mauricio" %% "postgresql-async"          % mauricioVersion    % "provided",
          "com.github.mauricio" %% "mysql-async"               % mauricioVersion    % "provided",
          "postgresql"          %  "postgresql"                % "9.2-1002.jdbc4"   % "test",
          "mysql"               %  "mysql-connector-java"      % "5.1.25"           % "test",
          "org.scalatest"       %% "scalatest"                 % "1.9.1"            % "test"
        )
      },
      sbtPlugin := false,
      scalacOptions ++= _scalacOptions,
      publishMavenStyle := true,
      publishArtifact in Test := false,
      parallelExecution in Test := false,
      pomIncludeRepository := { x => false },
      pomExtra := _pomExtra
    )
  )

  def _publishTo(v: String) = {
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }
  val _resolvers = Seq(
    "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases",
    "sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
  )
  val _scalacOptions = Seq("-deprecation", "-unchecked")
  val _pomExtra = <url>http://seratch.github.com/scalikejdbc</url>
      <licenses>
        <license>
          <name>Apache License, Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:seratch/scalikejdbc-async.git</url>
        <connection>scm:git:git@github.com:seratch/scalikejdbc-async.git</connection>
      </scm>
      <developers>
        <developer>
          <id>seratch</id>
          <name>Kazuhuiro Sera</name>
          <url>http://seratch.net/</url>
        </developer>
      </developers>

}

