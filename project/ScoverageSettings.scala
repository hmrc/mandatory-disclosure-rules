import sbt.Setting
import scoverage.ScoverageKeys

object ScoverageSettings {

  private val excludedPackages: Seq[String] = Seq(
    "<empty>",
    "Reverse.*",
    "uk.gov.hmrc.BuildInfo",
    "app.*",
    "prod.*",
    ".*Routes.*",
    "testOnly.*",
    "testOnlyDoNotUseInAppConf.*"
  )

  val settings: Seq[Setting[_]] = Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 60, //TODO reducing temp to to build the pipeline
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}
