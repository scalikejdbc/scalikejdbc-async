lazy val _version = "0.13.1-SNAPSHOT"
lazy val scalikejdbcVersion = "3.4.0"
lazy val jasyncVersion = "1.0.12" // provided
lazy val postgresqlVersion = "42.2.6"
lazy val testContainer = "1.11.4"
val Scala212 = "2.12.10"
val Scala213 = "2.13.1"

crossScalaVersions := Seq(Scala213, Scala212)

lazy val unusedWarnings = Seq(
  "-Ywarn-unused:imports"
)

lazy val core = (project in file("core")).settings(
  organization := "org.scalikejdbc",
  name := "scalikejdbc-async",
  version := _version,
  scalaVersion := Scala212,
  crossScalaVersions := Seq(Scala213, Scala212),
  publishTo := _publishTo(version.value),
  publishMavenStyle := true,
  // avoid NoClassDefFoundError
  // https://github.com/testcontainers/testcontainers-java/blob/22030eace3f4bafc735ccb0e402c1202329a95d1/core/src/main/java/org/testcontainers/utility/MountableFile.java#L284
  // https://github.com/sbt/sbt/issues/4794
  fork in Test := true,
  mappings in (Compile, packageSrc) ++= (managedSources in Compile).value.map{ f =>
    // to merge generated sources into sources.jar as well
    (f, f.relativeTo((sourceManaged in Compile).value).get.getPath)
  },
  sourceGenerators in Compile += task{
    val dir = (sourceManaged in Compile).value / "scalikejdbc" / "async"
    CodeGenerator.generate.map { s =>
      val f = dir / s.name
      IO.write(f, s.code)
      f
    }
  },
  libraryDependencies := {
    Seq (
      "org.scala-lang.modules" %% "scala-java8-compat"                % "0.9.0",
      "org.scalikejdbc"        %% "scalikejdbc"                       % scalikejdbcVersion % "compile",
      "org.scalikejdbc"        %% "scalikejdbc-interpolation"         % scalikejdbcVersion % "compile",
      "org.scalikejdbc"        %% "scalikejdbc-syntax-support-macro"  % scalikejdbcVersion % "test",
      "org.scalikejdbc"        %% "scalikejdbc-joda-time"             % scalikejdbcVersion % "test",
      "com.github.jasync-sql"  %  "jasync-postgresql"                 % jasyncVersion      % "provided",
      "com.github.jasync-sql"  %  "jasync-mysql"                      % jasyncVersion      % "provided",
      "com.dimafeng"           %% "testcontainers-scala"              % "0.34.1"           % "test",
      "org.testcontainers"     %  "mysql"                             % testContainer      % "test",
      "org.testcontainers"     %  "postgresql"                        % testContainer      % "test",
      "org.postgresql"         %  "postgresql"                        % postgresqlVersion  % "test",
      "mysql"                  %  "mysql-connector-java"              % "5.1.+"            % "test",
      "org.scalatest"          %% "scalatest"                         % "3.1.0"            % "test",
      "ch.qos.logback"         %  "logback-classic"                   % "1.2.+"            % "test"
    )
  },
  sbtPlugin := false,
  transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-language:implicitConversions", "-feature") ++ unusedWarnings,
  Seq(Compile, Test).flatMap(
    c => scalacOptions in (c, console) --= unusedWarnings
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
