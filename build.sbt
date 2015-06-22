val appVersion = sys.env.getOrElse("SNAP_PIPELINE_COUNTER", "1.0.0-SNAPSHOT")

lazy val root = (project in file(".")).
  settings(
    name := "bubblewrap",
    version := appVersion,
    scalaVersion := "2.10.4"
  )

libraryDependencies += "com.ning" % "async-http-client" % "1.9.24"
libraryDependencies += "commons-io" % "commons-io" % "2.4"
libraryDependencies += "org.jsoup" % "jsoup" % "1.8.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % Test
libraryDependencies += "org.mockito" % "mockito-all" % "1.9.5" % Test

publishTo := Some("Indix Release Artifactory" at "http://artifacts.indix.tv:8081/artifactory/libs-release-local")
