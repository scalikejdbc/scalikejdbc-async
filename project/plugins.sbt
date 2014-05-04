addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

libraryDependencies <+= (sbtVersion){ sv =>
  sv.split('.') match { case Array("0", a, b, _@_*) =>
    if (a.toInt <= 10 || a.toInt <= 11 && b.toInt <= 2) "org.scala-tools.sbt" %% "scripted-plugin" % sv
    else if (a.toInt == 11) "org.scala-sbt" %% "scripted-plugin" % sv
    else "org.scala-sbt" % "scripted-plugin" % sv
  }
}

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.3")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.4")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3")

