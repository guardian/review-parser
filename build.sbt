
val AwsSdkVersion = "1.11.46"
val CirceVersion = "0.5.0-M2"
val CapiVersion = "10.16"

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
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % CirceVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-parser" % CirceVersion
    )
  )

lazy val restaurants = (project in file("restaurants"))
  .dependsOn(common)
  .settings(commonSettings)

lazy val games = (project in file("games"))
  .dependsOn(common)
  .settings(commonSettings)

lazy val films = (project in file("films"))
  .dependsOn(common)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.squareup.okhttp3" % "okhttp" % "3.4.2"
    )
  )

initialize := {
  val _ = initialize.value
  assert(sys.props("java.specification.version") == "1.8",
    "Java 8 is required for this project.")
}
