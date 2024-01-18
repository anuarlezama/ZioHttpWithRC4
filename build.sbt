ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "ZIOHttp"
  )

libraryDependencies += "dev.zio" %% "zio-http" % "3.0.0-RC4"
libraryDependencies ++= Seq(
  "io.getquill"   %% "quill-zio"      % "4.7.0",
  "io.getquill"   %% "quill-jdbc-zio" % "4.7.0",
  "com.h2database" % "h2"             % "2.2.224"
)
