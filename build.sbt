import scala.language.postfixOps

lazy val replicado = project in file(".")

name := "replicado"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

val http4sVersion = "0.15.13a"

libraryDependencies ++= Seq(
  "org.http4s" % "http4s-core_2.11" % http4sVersion withSources,
  "org.http4s" % "http4s-blaze-client_2.11" % http4sVersion withSources,
  "org.http4s" % "http4s-argonaut_2.11" % http4sVersion withSources,
  "io.argonaut" % "argonaut_2.11" % "6.2" withSources
)