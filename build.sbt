ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "url-shorter"
  )

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.9"
val RedisClientVersion = "3.41"
val logbackVersion = "1.2.3"
val typesafeScalaLoggingVersion = "3.7.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "net.debasishg" %% "redisclient" % RedisClientVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % typesafeScalaLoggingVersion
)

//libraryDependencies += "redis.clients" % "jedis" % "2.0.0"
