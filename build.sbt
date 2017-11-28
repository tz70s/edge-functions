// Some common settings
lazy val commonSettings = Seq(
  organization := "org.dsngroup",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.4",
  javaOptions := Seq("-source", "1.8", "-target", "1.8")
)

lazy val invoker = project.in(file("invoker"))
    .settings(
      commonSettings,
      name := "invoker",
      description := "The invocation agent sits in edge devices",
      libraryDependencies ++= Seq(
        "com.spotify" % "docker-client" % "8.9.2",
        "com.sandinh" %% "paho-akka" % "1.5.0",
        "com.typesafe.akka" %% "akka-http" % "10.0.10",
        "com.typesafe.akka" %% "akka-actor" % "2.5.4",
        "com.typesafe.akka" %% "akka-stream" % "2.5.4",
        "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.10"
      )
    )

lazy val rootProject = Project("edge-functions", file("."))
    .settings(
      commonSettings,
      name := "edge-functions",
      description := "Function-as-a-Service in IoT edge environment"
    )
