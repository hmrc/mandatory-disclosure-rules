import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, integrationTestSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "mandatory-disclosure-rules"

val silencerVersion = "1.7.6"


lazy val scalaCompilerOptions = Seq(
  "-Xlint:-missing-interpolator,_",
  "-Yno-adapted-args",
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
    majorVersion                     := 0,
    scalaVersion                     := "2.12.15",
    PlayKeys.playDefaultPort := 10019,
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    scalafmtOnCompile in Compile := true,
    scalafmtOnCompile in Test := true,
    scalafmtOnCompile.withRank(KeyRanks.Invisible) in ThisBuild := true,
    scalacOptions ++= scalaCompilerOptions,
    // ***************
    // Use the silencer plugin to suppress warnings
    scalacOptions += "-P:silencer:pathFilters=routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
    // ***************
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(ScoverageSettings.settings: _*)
  .settings(integrationTestSettings(): _*)
  .settings(addTestReportOption(Test, "test-reports"))
  .settings(resolvers += Resolver.jcenterRepo)
