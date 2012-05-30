import sbt._
import Keys._

object Build extends Build {
  lazy val root = Project(id = "Akka_Play_mini", 
  	base = file("."), settings = Project.defaultSettings).settings(
    resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Typesafe Snapshot Repo" at "http://repo.typesafe.com/typesafe/snapshots/",
	libraryDependencies += "com.typesafe" %% "play-mini" % "2.0",
    libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.1",
    libraryDependencies += "com.typesafe.akka" % "akka-remote" % "2.0.1",
	libraryDependencies += "com.mongodb.casbah" % "casbah_2.9.0-1" % "2.1.5.0",
    mainClass in (Compile, run) := Some("play.core.server.NettyServer"))
}
