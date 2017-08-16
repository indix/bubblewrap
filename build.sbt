val asyncHttpClient =  "org.asynchttpclient" % "async-http-client" % "2.0.29"
val commonsIo = "commons-io" % "commons-io" % "2.4"
val jsoup = "org.jsoup" % "jsoup" % "1.8.1"
val tika = "org.apache.tika" % "tika-core" % "1.4"

val mockito = "org.mockito" % "mockito-all" % "1.9.5" % Test
val scalatest = "org.scalatest" %% "scalatest" % "2.2.4" % Test

val appMajorVersion = "0.2."
val appVersion = (Option(System.getenv("TRAVIS_TAG")) orElse Option(System.getenv("TRAVIS_BUILD_NUMBER")).map(appMajorVersion + _ + "-SNAPSHOT")).getOrElse("1.0.0-SNAPSHOT")

def parserCombinator(version:String) :Option[ModuleID] = version match {
  case "2.11.11" | "2.12.3" => Some("org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5")
  case  _ => None
}

lazy val commonSettings = Seq(
  organization := "com.indix",
  organizationName := "Indix",
  organizationHomepage := Some(url("http://www.indix.com")),
  version := {
    println("Setting the version of the app to - " + appVersion)
    appVersion
  },
  scalaVersion := "2.10.4",
  crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.3"),
  parallelExecution in This := false,
  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
  javacOptions ++= Seq("-Xlint:deprecation", "-source", "1.7"),
  libraryDependencies ++= Seq(
    asyncHttpClient, commonsIo, jsoup, mockito, scalatest, tika
  ) ++ Seq(scalaVersion(parserCombinator).value).flatten,
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra :=
      <url>https://github.com/indix/bubblewrap</url>
      <licenses>
        <license>
          <name>Apache License</name>
          <url>https://raw.githubusercontent.com/ind9/bubblewrap/master/LICENSE</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:ind/bubblewrap.git</url>
        <connection>scm:git:git@github.com:ind9/bubblewrap.git</connection>
      </scm>
      <developers>
        <developer>
          <id>indix</id>
          <name>Indix</name>
          <url>http://www.indix.com</url>
        </developer>
      </developers>
) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

lazy val bubblewrap = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "bubblewrap"
  )