name := "84-code"

version := "0.1"

scalaVersion := "2.12.8"


libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.8"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.22"

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8"

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.19" % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % "10.1.8" % "test"
)