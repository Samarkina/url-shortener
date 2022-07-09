ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "url-shortener"
  )

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.9"
val RedisClientVersion = "3.41"
val logbackVersion = "1.2.3"
val typesafeScalaLoggingVersion = "3.7.2"
val scalaTestVersion = "3.2.12"
val embeddedRedisVersion = "0.4.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % "test",

  "net.debasishg" %% "redisclient" % RedisClientVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.scalactic" %% "scalactic" % scalaTestVersion % "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",

  "com.github.sebruck" %% "scalatest-embedded-redis" % embeddedRedisVersion % "test",
)
