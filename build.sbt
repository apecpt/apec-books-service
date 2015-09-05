
val playJsonVersion = "2.4.2"
val akkaV = "2.3.11"
val sprayV = "1.3.3"

val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaV

val baseSettings = Seq(
	organization := "pt.org.apec",
	version := "0.1",
	scalaVersion  := "2.11.6",
	scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"))

val commonSettings = baseSettings ++ Seq(
libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7",
    "com.typesafe.play" %% "play-json" % playJsonVersion))

val clientSettings = baseSettings ++ Seq(
	libraryDependencies ++= Seq(akkaActor % "provided",
	"io.spray" %% "spray-client" % "1.3.3")
)

val serviceLibraryDependencies = {
	val slickV = "3.0.0"
	Seq(akkaActor,
  "io.spray" %% "spray-can" % sprayV,
"io.spray" %% "spray-client" % sprayV,
  "io.spray" %% "spray-routing" % sprayV,
  "com.typesafe.slick" %% "slick" % slickV,
  "io.spray" %% "spray-testkit" % sprayV,
"org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.postgresql" % "postgresql" % "9.4-1201-jdbc4",
"com.zaxxer" % "HikariCP" % "2.3.7",
"ch.qos.logback" % "logback-classic" % "1.1.3" % "runtime",
"com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.0.0"
)
}

lazy val root = (project in file("."))
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
	.dependsOn(common, client)
.aggregate(common,client)

lazy val common = (project in file("common"))
	.settings(commonSettings : _*)
	.settings(name := "apec-books-common")

lazy val client = (project in file("client"))
	.settings(clientSettings : _*)
	.settings(name := "apec-books-client")
	.dependsOn(common)


