addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform"      % "1.3.0")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")
addSbtPlugin("org.scoverage"    % "sbt-scoverage"        % "1.0.4")
addSbtPlugin("org.scoverage"    % "sbt-coveralls"        % "1.0.0.BETA1")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
addSbtPlugin("com.typesafe.play" % "sbt-plugin"       % "2.4.0-M2")
addSbtPlugin("com.typesafe.sbt"  % "sbt-coffeescript" % "1.0.0")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

