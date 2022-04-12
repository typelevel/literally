ThisBuild / tlBaseVersion := "1.0"

ThisBuild / developers += tlGitHubDev("mpilquist", "Michael Pilquist")
ThisBuild / startYear := Some(2021)

ThisBuild / crossScalaVersions := List("2.12.15", "2.13.8", "3.1.2")
ThisBuild / tlVersionIntroduced := Map("3" -> "1.0.2")

lazy val root = tlCrossRootProject.aggregate(core, tests)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .settings(
    name := "literally",
    scalacOptions := scalacOptions.value.filterNot(_ == "-source:3.0-migration"),
    libraryDependencies ++= {
      if (tlIsScala3.value) Nil else List("org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided)
    }
  )

lazy val tests = crossProject(JSPlatform, JVMPlatform)
  .enablePlugins(NoPublishPlugin)
  .dependsOn(core)
  .settings(
    name := "tests",
    scalacOptions := scalacOptions.value.filterNot(_ == "-source:3.0-migration"),
    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.29" % Test,
    libraryDependencies ++= {
      if (tlIsScala3.value) Nil else List("org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided)
    }
  )
