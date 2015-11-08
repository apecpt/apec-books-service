
val playJsonVersion = "2.4.2"
val akkaV = "2.3.12"
val sprayV = "1.3.3"
val godivaVersion = "0.1.0"

val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaV

val baseSettings = Seq(
	organization := "pt.org.apec",
	version := "0.1",
	scalaVersion  := "2.11.7",
	scalacOptions in Compile ++= Seq(
    "-encoding", "UTF-8",
    "-deprecation",
    "-unchecked",
    "-feature",
    "-Xlint",
    "-Ywarn-unused-import",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-Xmax-classfile-name", "255" //due to pickling macros
))

val commonSettings = baseSettings ++ Seq(
libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7",
    "com.typesafe.play" %% "play-json" % playJsonVersion))


val serviceLibraryDependencies = {
	val slickV = "3.1.0"
	Seq(akkaActor,
  "io.spray" %% "spray-can" % sprayV,
"io.spray" %% "spray-client" % sprayV,
  "io.spray" %% "spray-routing-shapeless2" % sprayV,
  "com.typesafe.slick" %% "slick" % slickV,
  "io.spray" %% "spray-testkit" % sprayV,
  "com.typesafe.slick" %% "slick-hikaricp" % slickV,
"org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.postgresql" % "postgresql" % "9.4-1201-jdbc4",
    "ch.qos.logback" % "logback-classic" % "1.1.3" % "runtime",
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.1.0",
  "com.ruiandrebatista.godiva" %% "godiva-slick" % godivaVersion,
  "com.ruiandrebatista.godiva" %% "godiva-spray" % godivaVersion,
  "com.ruiandrebatista.godiva" %% "godiva-play-json" % godivaVersion
)
}

lazy val root = (project in file("."))
	.aggregate(common, service)

lazy val service = (project in file("service"))
	.settings(commonSettings : _*)
	.settings(name := "apec-books-service")
	.settings(libraryDependencies ++= serviceLibraryDependencies)
	.settings(
resolvers += "softprops-maven" at "http://dl.bintray.com/content/softprops/maven",
scalacOptions in Test ++= Seq ( "-Yrangepos"),
parallelExecution in Test := false,
fork in run := true,
dockerBaseImage := "java:8")
.settings(
Revolver.settings :_*)
.enablePlugins (JavaAppPackaging, DockerPlugin)
	.dependsOn(common)

lazy val common = (project in file("common"))
	.settings(commonSettings : _*)
	.settings(name := "apec-books-common")


