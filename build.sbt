lazy val _version = "0.11.0"
lazy val scalikejdbcVersion = "3.3.0"
lazy val mauricioVersion = "0.2.21" // provided
lazy val postgresqlVersion = "42.2.2"
val Scala211 = "2.11.12"
val Scala212 = "2.12.6"

crossScalaVersions := Seq(Scala212, Scala211)

lazy val core = (project in file("core")).settings(
  organization := "org.scalikejdbc",
  name := "scalikejdbc-async",
  version := _version,
  scalaVersion := Scala211,
  crossScalaVersions := Seq(Scala212, Scala211),
  publishTo := _publishTo(version.value),
  publishMavenStyle := true,
  resolvers ++= _resolvers,
  libraryDependencies := {
    Seq (
       "org.scalikejdbc"     %% "scalikejdbc"                       % scalikejdbcVersion % "compile",
       "org.scalikejdbc"     %% "scalikejdbc-interpolation"         % scalikejdbcVersion % "compile",
       "org.scalikejdbc"     %% "scalikejdbc-syntax-support-macro"  % scalikejdbcVersion % "compile",
       "org.scalikejdbc"     %% "scalikejdbc-joda-time"             % scalikejdbcVersion % "test",
       "com.github.mauricio" %% "postgresql-async"                  % mauricioVersion    % "provided",
       "com.github.mauricio" %% "mysql-async"                       % mauricioVersion    % "provided",
       "org.postgresql"      %  "postgresql"                        % postgresqlVersion  % "test",
       "mysql"               %  "mysql-connector-java"              % "5.1.+"            % "test",
       "org.scalatest"       %% "scalatest"                         % "3.0.+"            % "test",
       "ch.qos.logback"      %  "logback-classic"                   % "1.2.+"            % "test"
    )
  },
  sbtPlugin := false,
  transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
  scalacOptions ++= _scalacOptions,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  parallelExecution in Test := false,
  pomIncludeRepository := { x => false },
  pomExtra := _pomExtra
)

def _publishTo(v: String) = {
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
val _resolvers = Seq(
  "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases"
)
val _scalacOptions = Seq("-deprecation", "-unchecked", "-Xfuture")
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
