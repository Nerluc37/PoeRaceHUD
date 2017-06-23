name := "RaceStats"

version := "1.0"

scalaVersion := "2.12.1"

resolvers += "Tim Tennant's repo" at "http://dl.bintray.com/timt/repo/"

libraryDependencies += "io.shaka" %% "naive-http" % "94"

libraryDependencies += "io.circe" %% "circe-parser" % "0.8.0"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.13"

libraryDependencies += "com.typesafe" % "config" % "1.3.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % "0.8.0",
  "io.circe" %% "circe-generic" % "0.8.0",
  "io.circe" %% "circe-jawn" % "0.8.0"
)
