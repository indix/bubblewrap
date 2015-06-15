lazy val root = (project in file(".")).
  settings(
    name := "bubblewrap",
    version := "1.0",
    scalaVersion := "2.10.4"
  )

libraryDependencies += "com.ning" % "async-http-client" % "1.9.24"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

libraryDependencies += "org.mockito" % "mockito-all" % "1.9.5"

libraryDependencies += "org.jsoup" % "jsoup" % "1.8.1"

publishTo := Some("Indix Release Artifactory" at "http://artifacts.indix.tv:8081/artifactory/libs-release-local")