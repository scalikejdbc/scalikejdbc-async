lazy val _version = "0.7.0"
lazy val scalikejdbcVersion = "2.5.0"
lazy val mauricioVersion = "0.2.21" // provided
lazy val postgresqlVersion = "9.4-1201-jdbc41"
lazy val defaultPlayVersion = play.core.PlayVersion.current
val Scala210 = "2.10.6"
val Scala211 = "2.11.8"
val Scala212 = "2.12.1"

crossScalaVersions := Seq(Scala212, Scala211, Scala210)

lazy val core = (project in file("core")).settings(
  organization := "org.scalikejdbc",
  name := "scalikejdbc-async",
  version := _version,
  scalaVersion := Scala211,
  crossScalaVersions := Seq(Scala212, Scala211, Scala210),
  publishTo := _publishTo(version.value),
  publishMavenStyle := true,
  resolvers ++= _resolvers,
  libraryDependencies := {
    Seq (
       "org.scalikejdbc"     %% "scalikejdbc"               % scalikejdbcVersion % "compile",
       "org.scalikejdbc"     %% "scalikejdbc-interpolation" % scalikejdbcVersion % "compile",
       "com.github.mauricio" %% "postgresql-async"          % mauricioVersion    % "provided",
       "com.github.mauricio" %% "mysql-async"               % mauricioVersion    % "provided",
       "org.postgresql"      %  "postgresql"                % postgresqlVersion  % "test",
       "mysql"               %  "mysql-connector-java"      % "5.1.+"            % "test",
       "org.scalatest"       %% "scalatest"                 % "3.0.+"            % "test",
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

lazy val playPlugin = (project in file("play-plugin")).settings(
  sbtPlugin := false,
  organization := "org.scalikejdbc",
  name := "scalikejdbc-async-play-plugin",
  version := _version,
  scalaVersion := Scala211,
  crossScalaVersions := Seq(Scala211, Scala210),
  resolvers ++= _resolvers,
  resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  libraryDependencies ++= Seq(
    "com.github.mauricio"    %% "postgresql-async" % mauricioVersion    % "provided",
    "com.github.mauricio"    %% "mysql-async"      % mauricioVersion    % "provided",
    "com.typesafe.play"      %% "play"             % defaultPlayVersion % "provided",
    "com.typesafe.play"      %% "play-test"        % defaultPlayVersion % "test"
  ),
  testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "sequential", "true"),
  publishTo := _publishTo(version.value),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { x => false },
  pomExtra := _pomExtra,
  transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
  incOptions := incOptions.value.withNameHashing(true),
  scalacOptions ++= _scalacOptions
) dependsOn(core)

lazy val playSample = (project in file("play-sample")).enablePlugins(play.PlayScala).settings(
  scalaVersion := Scala211,
  libraryDependencies ++= Seq(
   "org.scalikejdbc"      %% "scalikejdbc"                     % scalikejdbcVersion,
    "org.scalikejdbc"      %% "scalikejdbc-config"              % scalikejdbcVersion,
    "org.scalikejdbc"      %% "scalikejdbc-interpolation"       % scalikejdbcVersion,
    "com.github.mauricio"  %% "postgresql-async"                % mauricioVersion,
    "com.github.mauricio"  %% "mysql-async"                     % mauricioVersion,
    "org.postgresql"       %  "postgresql"                      % postgresqlVersion,
    "com.github.tototoshi" %% "play-flyway"                     % "1.2.+",
    "mysql"                %  "mysql-connector-java"            % "5.1.+",
    "org.json4s"           %% "json4s-ext"                      % "3.4.+",
    "com.github.tototoshi" %% "play-json4s-native"              % "0.3.+"
  ),
  resolvers ++= _resolvers
).dependsOn(core, playPlugin)

def _publishTo(v: String) = {
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
val _resolvers = Seq(
  "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases"
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
