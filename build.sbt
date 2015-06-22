val asyncHttpClient =  "com.ning" % "async-http-client" % "1.9.24"
val commonsIo = "commons-io" % "commons-io" % "2.4"
val jsoup = "org.jsoup" % "jsoup" % "1.8.1"

val mockito = "org.mockito" % "mockito-all" % "1.9.5" % Test
val scalatest = "org.scalatest" %% "scalatest" % "2.2.4" % Test

val appVersion = sys.env.getOrElse("SNAP_PIPELINE_COUNTER", "1.0.0-SNAPSHOT")

lazy val commonSettings = Seq(
  organization := "com.indix.cannonball",
  version := appVersion,
  scalaVersion := "2.10.4",
  crossPaths := false,
  parallelExecution in This := false,
  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
  javacOptions ++= Seq("-Xlint:deprecation", "-source", "1.7"),
  libraryDependencies ++= Seq(
    asyncHttpClient, commonsIo, jsoup, mockito, scalatest
  )
) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

lazy val bubblewrap = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "bubblewrap"
  )

publishTo := Some("Indix Release Artifactory" at "http://artifacts.indix.tv:8081/artifactory/libs-release-local")
