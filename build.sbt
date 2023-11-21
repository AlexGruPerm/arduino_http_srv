name := "ws_arduino_wifi"

ThisBuild / organization := "yakushev"
ThisBuild / version      := "0.2.0"
ThisBuild / scalaVersion := "2.13.6"

// PROJECTS
lazy val global = project
  .in(file("."))
  .settings(commonSettings)
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    ws
  )

lazy val ws = (project in file("ws"))
  .settings(
    assembly / assemblyJarName := "ws.jar",
    name := "ws",
    commonSettings,
    libraryDependencies ++= commonDependencies
  )

lazy val dependencies =
  new {
  val akkaV      = "2.6.15"
  val akkaHttpV  = "10.2.5"
  val circeV     = "0.14.1"
  val logbackV   = "1.2.3"

  val logback        = "ch.qos.logback" % "logback-classic" % logbackV

  val akkaHttp       = "com.typesafe.akka" %% "akka-http" % akkaHttpV
  val akkaActor      = "com.typesafe.akka" %% "akka-actor" % akkaV
  val akkaStream     = "com.typesafe.akka" %% "akka-stream" % akkaV
  val akkaActorTyped = "com.typesafe.akka" %% "akka-actor-typed" % akkaV
  val akkaSlf4j      = "com.typesafe.akka" %% "akka-slf4j" % akkaV

  val circeCore      = "io.circe" %% "circe-core" % circeV
  val circeGeneric   = "io.circe" %% "circe-generic" % circeV
  val circeParser    = "io.circe" %% "circe-parser" % circeV
  val circeLiteral   = "io.circe" %% "circe-literal" % circeV

  val akka = List(akkaHttp, akkaActor, akkaStream, akkaActorTyped, akkaSlf4j)
  val circe = List(circeCore, circeGeneric, circeParser, circeLiteral)

  }

val commonDependencies = {
  dependencies.akka ++
  dependencies.circe
}

lazy val compilerOptions = Seq(
        "-Ymacro-annotations",
        "-deprecation",
        "-encoding", "utf-8",
        "-explaintypes",
        "-feature",
        "-unchecked",
        "-language:postfixOps",
        "-language:higherKinds",
        "-language:implicitConversions",
        "-Xcheckinit",
        "-Xfatal-warnings"
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      Resolver.mavenLocal,
      Resolver.sonatypeRepo("public")
  )
)


ws / assembly / assemblyMergeStrategy := {
  case PathList("module-info.class") => MergeStrategy.discard
  case x if x.endsWith("/module-info.class") => MergeStrategy.discard
  case PathList("META-INF", xs @ _*)         => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}