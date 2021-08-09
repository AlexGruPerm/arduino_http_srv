name := "web_serv_api_core"

ThisBuild / organization := "yakushev"
ThisBuild / version      := "0.1.0"
ThisBuild / scalaVersion := "2.13.6"

// PROJECTS
lazy val global = project
  .in(file("."))
  .settings(commonSettings)
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    core,
    api
  )

lazy val core = (project in file("core"))
  .settings(
    assembly / assemblyJarName := "core.jar",
    name := "core",
    commonSettings,
    libraryDependencies ++= commonDependencies
  )

lazy val api = (project in file("api"))
  .settings(
    assembly / assemblyJarName := "api.jar",
    name := "api",
    commonSettings,
    libraryDependencies ++= commonDependencies
  )

lazy val dependencies =
  new {
  val akkaV      = "2.6.15"
  val akkaHttpV  = "10.2.5"
  val circeV     = "0.14.1"
  val logbackV   = "1.2.3"
  val elastic4sV = "7.13.0"
  val elastic4sHttpV = "6.7.8"

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

  val elastic4sClient = "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % elastic4sV
  val elastic4sHttp   = "com.sksamuel.elastic4s" %% "elastic4s-http" % elastic4sHttpV

  val elastic4s = List(elastic4sClient, elastic4sHttp)
  val akka = List(akkaHttp, akkaActor, akkaStream, akkaActorTyped, akkaSlf4j)
  val circe = List(circeCore, circeGeneric, circeParser, circeLiteral)

  }

val commonDependencies = {
  List(dependencies.logback) ++
  dependencies.elastic4s ++
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

api / assembly / assemblyMergeStrategy := {
  case PathList("module-info.class") => MergeStrategy.discard
  case x if x.endsWith("/module-info.class") => MergeStrategy.discard
  case PathList("META-INF", xs @ _*)         => MergeStrategy.discard
  case _ => MergeStrategy.first
}

core / assembly / assemblyMergeStrategy := {
  case PathList("module-info.class") => MergeStrategy.discard
  case x if x.endsWith("/module-info.class") => MergeStrategy.discard
  case PathList("META-INF", xs @ _*)         => MergeStrategy.discard
  case _ => MergeStrategy.first
}