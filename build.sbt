
val AwsSdkVersion = "1.11.46"

val CapiVersion = "10.10"

val commonSettings = Seq(
  scalaVersion := "2.11.8",
  organization := "com.gu",
  description := "Extracts, where it can, structured data from Guardian review articles.",
  scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-target:jvm-1.8", "-Xfatal-warnings"),
  scalacOptions in doc in Compile := Nil,
  resolvers += "Guardian GitHub Repository" at "http://guardian.github.io/maven/repo-releases",
  resolvers += Resolver.sonatypeRepo("releases"),
  libraryDependencies ++= Seq(
    "org.jsoup" % "jsoup" % "1.9.2",
    "com.typesafe" % "config" % "1.3.1",
    "com.gu" %% "content-api-client" % CapiVersion,
    "com.amazonaws" % "aws-java-sdk-sts" % AwsSdkVersion,
    "com.amazonaws" % "aws-java-sdk-kinesis" % AwsSdkVersion,
    "com.google.maps" % "google-maps-services" % "0.1.16",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "com.gu" % "content-api-models-json" % CapiVersion % Test,
    "org.scalatest" %% "scalatest" % "2.2.6" % Test
  )
)

lazy val root = (project in file("."))
  .aggregate(restaurants, games, common)

lazy val common = (project in file("common"))
  .settings(commonSettings)

lazy val restaurants = (project in file("restaurants"))
  .dependsOn(common)
  .settings(commonSettings)
  .settings(Seq(name := "restaurants"))

lazy val games = (project in file("games"))
  .dependsOn(common)
  .settings(commonSettings)
  .settings(Seq(name := "games"))

initialize := {
  val _ = initialize.value
  assert(sys.props("java.specification.version") == "1.8",
    "Java 8 is required for this project.")
}
