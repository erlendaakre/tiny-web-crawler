name := "tiny-web-crawler"
version := "0.1"
scalaVersion := "2.13.5"

val zioVersion = "2.0.0-M4"

libraryDependencies += "dev.zio" %% "zio" % zioVersion
//libraryDependencies += "dev.zio" %% "zio-streams" % "1.0.7"
//libraryDependencies += "dev.zio" %% "zio-process" % "0.3.0"
//libraryDependencies += "dev.zio" %% "zio-json" % "0.1.4"
//libraryDependencies += "io.d11" % "zhttp" % "1.0.0-SNAPSHOT-RC10"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.7" % "test"