val akkaVersion = "2.7.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "reactive-messaging-patterns",
    version := "0.1",
    scalaVersion := "2.13.10",
    organization := "com.github.yoshiyoshifujii",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j"       % akkaVersion,
      "ch.qos.logback"     % "logback-classic"  % "1.4.4" excludeAll (
        ExclusionRule(organization = "org.slf4j")
      ),
      "org.scalatest"     %% "scalatest"                % "3.2.14"     % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test
    )
  )
