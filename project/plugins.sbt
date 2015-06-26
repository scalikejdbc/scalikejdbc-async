addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform"      % "1.3.0")
addSbtPlugin("org.scoverage"    % "sbt-scoverage"        % "1.2.0")
addSbtPlugin("org.scoverage"    % "sbt-coveralls"        % "1.0.0")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
addSbtPlugin("com.typesafe.play" % "sbt-plugin"       % "2.4.1")
addSbtPlugin("com.typesafe.sbt"  % "sbt-coffeescript" % "1.0.0")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

