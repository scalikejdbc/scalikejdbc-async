import play.core.PlayVersion.{current => playV}
import sbt.Keys._

name := "scalikejdbc-async sample"
organization := "com.micronautics"

scalaVersion := "2.11.5"
version := "0.1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  jdbc,
  "com.typesafe.play"   %% "play"                          % playV withSources(),
  "com.typesafe.play"   %% "play-json"                     % playV withSources(),
  "org.webjars"         %% "webjars-play"                  % "2.3.0-2"   withSources(),
  "org.scalikejdbc"     %% "scalikejdbc-async"             % "0.5.+",
  "com.github.mauricio" %% "postgresql-async"              % "0.2.+",
  "org.scalikejdbc"     %% "scalikejdbc-async-play-plugin" % "0.5.+",
  "org.webjars"         %  "bootstrap"                     % "3.3.2",
  //
  "org.scalatestplus"   %% "play"                          % "1.2.0" % "test",
  "org.scalatest"       %% "scalatest"                     % "2.2.1" % "test" withSources(),
  "junit"               %  "junit"                         % "4.12"  % "test"
)

//scalariformSettings

//net.virtualvoid.sbt.graph.Plugin.graphSettings

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.7", "-unchecked",
  "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.7", "-target", "1.7", "-g:vars")

updateOptions := updateOptions.value.withCachedResolution(true)

doc in Compile <<= target.map(_ / "none")

publishArtifact in (Compile, packageSrc) := false

logBuffered in Test := false

Keys.fork in Test := false

parallelExecution in Test := false

// define the statements initially evaluated when entering 'console', 'console-quick' but not 'console-project'
initialCommands in console := """ // make app resources accessible
   |Thread.currentThread.setContextClassLoader(getClass.getClassLoader)
   |new play.core.StaticApplication(new java.io.File("."))
   |import java.net.URL
   |import java.text.DateFormat
   |import java.util.Locale
   |import org.joda.time._
   |import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
   |import play.api.db.DB
   |import play.api.libs.json._
   |import play.api.Play.current
   |import play.Logger
   |import scala.reflect.runtime.universe._
   |""".stripMargin

logLevel := Level.Warn

logLevel in test := Level.Info // Level.Info is needed to see detailed output when running tests

logLevel in compile := Level.Warn
