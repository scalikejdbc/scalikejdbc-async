addSbtPlugin("org.scalariform"  % "sbt-scalariform" % "1.6.0")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// TODO: Play 2.4 support
addSbtPlugin("com.typesafe.play" % "sbt-plugin"       % "2.3.10")
addSbtPlugin("com.typesafe.sbt"  % "sbt-coffeescript" % "1.0.0")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

addSbtPlugin("com.eed3si9n" % "sbt-doge" % "0.1.5")

addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC3")
// https://github.com/coursier/coursier/issues/450
classpathTypes += "maven-plugin"
