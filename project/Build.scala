import sbt._
import Keys._
import play.Project._

object ScalikeJDBCAsyncProject extends Build {

  lazy val _version = "0.4.0-RC"
  lazy val scalikejdbcVersion = "2.0.0-RC3"
  // TODO Scala 2.11 https://github.com/mauricio/postgresql-async/pull/87
  lazy val mauricioVersion = "0.2.13"
  lazy val defaultPlayVersion = "2.2.3"

  lazy val core = Project(
    id = "core",
    base = file("core"),
    settings = Defaults.defaultSettings ++ Seq(
      organization := "org.scalikejdbc",
      name := "scalikejdbc-async",
      version := _version,
      scalaVersion := "2.10.3",
      publishTo <<= version { (v: String) => _publishTo(v) },
      publishMavenStyle := true,
      resolvers ++= _resolvers,
      libraryDependencies <++= (scalaVersion) { scalaVersion =>
        Seq(
          "org.scalikejdbc"     %% "scalikejdbc"               % scalikejdbcVersion % "compile",
          "org.scalikejdbc"     %% "scalikejdbc-interpolation" % scalikejdbcVersion % "compile",
          "com.github.mauricio" %% "postgresql-async"          % mauricioVersion    % "provided",
          "com.github.mauricio" %% "mysql-async"               % mauricioVersion    % "provided",
          "org.postgresql"      %  "postgresql"                % "9.3-1101-jdbc41"  % "test",
          "mysql"               %  "mysql-connector-java"      % "5.1.+"            % "test",
          "org.scalatest"       %% "scalatest"                 % "2.1.+"            % "test",
          "ch.qos.logback"      %  "logback-classic"           % "1.1.+"            % "test"
        )
      },
      sbtPlugin := false,
      transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
      incOptions := incOptions.value.withNameHashing(true),
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
      organization := "org.scalikejdbc",
      name := "scalikejdbc-async-play-plugin",
      version := _version,
      scalaVersion := "2.10.3",
      resolvers ++= _resolvers,
      resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      libraryDependencies <++= (scalaVersion) { scalaVersion =>
        Seq(
          "com.github.mauricio"    %% "postgresql-async" % mauricioVersion    % "provided",
          "com.github.mauricio"    %% "mysql-async"      % mauricioVersion    % "provided",
          "com.typesafe.play"      %% "play"             % defaultPlayVersion % "provided",
          "com.typesafe.play"      %% "play-test"        % defaultPlayVersion % "test")
      },
      testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "sequential", "true"),
      publishTo <<= version { (v: String) => _publishTo(v) },
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { x => false },
      pomExtra := _pomExtra,
      transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
      incOptions := incOptions.value.withNameHashing(true),
      scalacOptions ++= _scalacOptions
    )
  ) dependsOn(core)

  lazy val playSample = {
    val appName         = "play-sample"
    val appVersion      = "0.1"
    val appDependencies = Seq(
      "org.scalikejdbc"      %% "scalikejdbc"                     % scalikejdbcVersion,
      "org.scalikejdbc"      %% "scalikejdbc-config"              % scalikejdbcVersion,
      "org.scalikejdbc"      %% "scalikejdbc-interpolation"       % scalikejdbcVersion,
      "com.github.mauricio"  %% "postgresql-async"                % mauricioVersion,
      "com.github.mauricio"  %% "mysql-async"                     % mauricioVersion,
      "org.postgresql"       %  "postgresql"                      % "9.3-1101-jdbc41",
      "com.github.tototoshi" %% "play-flyway"                     % "1.0.+",
      "mysql"                %  "mysql-connector-java"            % "5.1.+",
      "org.json4s"           %% "json4s-ext"                      % "3.2.+",
      "com.github.tototoshi" %% "play-json4s-native"              % "0.2.+"
    )
    play.Project(appName, appVersion, appDependencies, path = file("play-sample")).settings(
      scalaVersion in ThisBuild := "2.10.3",
      resolvers ++= Seq(
        "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases",
        "sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
      )
    ).dependsOn(core, playPlugin)
  }

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
          <url>http://seratch.net/</url>
        </developer>
      </developers>

}

