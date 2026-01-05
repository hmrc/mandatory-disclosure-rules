import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, itSettings}

val appName = "mandatory-disclosure-rules"

ThisBuild / scalaVersion := "3.3.6"
ThisBuild / majorVersion := 0

val silencerVersion = "1.7.6"

lazy val scalaCompilerOptions = Seq(
  "-Xlint:-missing-interpolator,_",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:privates",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:explicits",
  "-Ywarn-unused:implicits",
  "-Ywarn-value-discard",
  "-Ywarn-unused:patvars",
  "-Ywarn-dead-code",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-Xlint:-byname-implicit"
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    PlayKeys.playDefaultPort := 10019,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    dependencyOverrides ++= AppDependencies.overrides,
    Compile / scalafmtOnCompile                                := true,
    Test / scalafmtOnCompile                                   := true,
    ThisBuild / scalafmtOnCompile.withRank(KeyRanks.Invisible) := true,
    scalacOptions ++= scalaCompilerOptions,
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s",
      "-Wconf:src=.+/test/.+:s",
      "-Wconf:cat=deprecation&msg=\\.*()\\.*:s",
      "-Wconf:cat=unused-imports&site=<empty>:s",
      "-Wconf:cat=unused&src=.*RoutesPrefix\\.scala:s",
      "-Wconf:cat=unused&src=.*Routes\\.scala:s",
      "-Wconf:msg=unused import&src=html/.*:s",
      "-Wconf:msg=Flag.*repeatedly:s"
    )
  )
  .settings(ScoverageSettings.settings: _*)
  .settings(addTestReportOption(Test, "test-reports"))
  .settings(resolvers += Resolver.jcenterRepo)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(itSettings())
  .settings(libraryDependencies ++= AppDependencies.test)
