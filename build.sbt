name := "apec-books-service"

organization := "pt.org.apec"

version := "0.1"

scalaVersion  := "2.11.6"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
	val akkaV = "2.3.11"
	val sprayV = "1.3.3"
	val slickV = "3.0.0"
	Seq("com.typesafe.akka" %% "akka-actor" % akkaV,
  "io.spray" %% "spray-can" % sprayV,
"io.spray" %% "spray-client" % sprayV,
  "io.spray" %% "spray-routing" % sprayV,
  "com.typesafe.slick" %% "slick" % slickV,
  "io.spray" %% "spray-testkit" % sprayV,
"org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
"com.zaxxer" % "HikariCP" % "2.3.7",
"ch.qos.logback" % "logback-classic" % "1.1.3" % "runtime",
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.0.0"
)

}

scalacOptions in Test ++= Seq ( "-Yrangepos")


Revolver.settings

enablePlugins (JavaAppPackaging, DockerPlugin ) 

dockerBaseImage := "java:8"
