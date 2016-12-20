addSbtPlugin("org.scalariform"  % "sbt-scalariform" % "1.6.0")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// TODO: Play 2.4 support
addSbtPlugin("com.typesafe.play" % "sbt-plugin"       % "2.3.10")
addSbtPlugin("com.typesafe.sbt"  % "sbt-coffeescript" % "1.0.0")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
