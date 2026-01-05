import play.core.PlayVersion
import sbt.*

object AppDependencies {
  val playVersion   = "play-30"
  val bootstrap_ver = "10.4.0"
  val mongo_ver     = "2.11.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% s"bootstrap-backend-$playVersion" % bootstrap_ver,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-$playVersion"        % mongo_ver,
    "org.typelevel"     %% "cats-core"                       % "2.10.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatestplus"    %% "scalacheck-1-18"               % "3.2.19.0",
    "io.github.wolfendale" %% "scalacheck-gen-regexp"         % "1.1.0",
    "uk.gov.hmrc"          %% s"bootstrap-test-$playVersion"  % bootstrap_ver,
    "uk.gov.hmrc.mongo"    %% s"hmrc-mongo-test-$playVersion" % mongo_ver,
    "org.scalatestplus"    %% "mockito-4-11"                  % "3.2.18.0"
  ).map(_ % Test)

  val overrides: Seq[ModuleID] = Seq(
    "com.fasterxml.jackson.core" % "jackson-core" % "2.20.1"
  )
}
