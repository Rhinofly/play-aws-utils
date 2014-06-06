name := "play-aws-utils"

version := "4.0.1"

organization := "nl.rhinofly"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ws" % "2.3.0",
  "com.typesafe.play" %% "play-test" % "2.3.0" % "test",
  "org.specs2" %% "specs2" % "2.3.12" % "test"
)

publishTo := {
    val repo = if (version.value endsWith "SNAPSHOT") "snapshot" else "release"
    Some("Rhinofly Internal " + repo.capitalize + " Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-" + repo + "-local")
  }

resolvers ++= Seq(
  "Typesafe Release Repository" at "http://repo.typesafe.com/typesafe/releases")  
  
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

scalacOptions += "-deprecation"
