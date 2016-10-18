organization := "com.gu"
name:= "review-extractor"
description := "Extracts, where it can, structured data from Guardian review articles."
scalaVersion := "2.11.8"
scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-target:jvm-1.8", "-Xfatal-warnings")
scalacOptions in doc in Compile := Nil
resolvers += "Guardian GitHub Repository" at "http://guardian.github.io/maven/repo-releases"

libraryDependencies ++= Seq(
  "org.jsoup" % "jsoup" % "1.9.2",
  "com.gu" %% "content-api-client" % "10.2",
  "com.google.maps" % "google-maps-services" % "0.1.16",
  "org.scalatest" %% "scalatest" % "2.2.6" % Test
)

initialize := {
  val _ = initialize.value
  assert(sys.props("java.specification.version") == "1.8",
    "Java 8 is required for this project.")
}