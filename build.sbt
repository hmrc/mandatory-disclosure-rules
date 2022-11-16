import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, integrationTestSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "mandatory-disclosure-rules"

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
  "-language:implicitConversions"
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion             := 0,
    scalaVersion             := "2.13.8",
    PlayKeys.playDefaultPort := 10019,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    Compile / scalafmtOnCompile                                := true,
    Test / scalafmtOnCompile                                   := true,
    ThisBuild / scalafmtOnCompile.withRank(KeyRanks.Invisible) := true,
    scalacOptions ++= scalaCompilerOptions,
    scalacOptions += "-Wconf:src=routes/.*:s"
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(ScoverageSettings.settings: _*)
  .settings(integrationTestSettings(): _*)
  .settings(addTestReportOption(Test, "test-reports"))
  .settings(resolvers += Resolver.jcenterRepo)
