scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

addSbtPlugin("org.scalariform"  % "sbt-scalariform" % "1.6.0")
// TODO: Play 2.4 support
addSbtPlugin("com.typesafe.play" % "sbt-plugin"       % "2.3.10")
addSbtPlugin("com.typesafe.sbt"  % "sbt-coffeescript" % "1.0.0")
addSbtPlugin("com.eed3si9n" % "sbt-doge" % "0.1.5")
