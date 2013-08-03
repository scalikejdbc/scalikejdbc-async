import sbt._
import Keys._
import play.Project._

object ScalikeJDBCAsyncProject extends Build {

  lazy val _version = "0.2.2-SNAPSHOT"
  lazy val defaultPlayVersion = "2.1.2"
  lazy val scalikejdbcVersion = "1.6.7"
  lazy val mauricioVersion = "0.2.4"

  lazy val core = Project(
    id = "core",
    base = file("core"),
    settings = Defaults.defaultSettings ++ Seq(
      organization := "com.github.seratch",
      name := "scalikejdbc-async",
      version := _version,
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
          "org.postgresql"      %  "postgresql"                % "9.2-1003-jdbc4"   % "test",
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

  lazy val playPlugin = Project(
    id = "play-plugin",
    base = file("play-plugin"),
    settings = Defaults.defaultSettings ++ Seq(
      sbtPlugin := false,
      organization := "com.github.seratch",
      name := "scalikejdbc-async-play-plugin",
      version := _version,
      scalaVersion := "2.10.0",
      resolvers ++= _resolvers,
      libraryDependencies <++= (scalaVersion) { scalaVersion =>
        Seq(
          "play" % "play_2.10"      % defaultPlayVersion % "provided",
          "play" % "play-test_2.10" % defaultPlayVersion % "test")
      },
      testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "sequential", "true"),
      publishTo <<= version { (v: String) => _publishTo(v) },
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { x => false },
      pomExtra := _pomExtra,
      scalacOptions ++= _scalacOptions
    )
  ) dependsOn(core)


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

