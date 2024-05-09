import play.core.PlayVersion
import sbt.*

object AppDependencies {
  val playVersion   = "play-30"
  val bootstrap_ver = "8.5.0"
  val mongo_ver     = "1.9.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% s"bootstrap-backend-$playVersion" % bootstrap_ver,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-$playVersion"        % mongo_ver,
    "org.julienrf"      %% "play-json-derived-codecs"        % "11.0.0",
    "org.typelevel"     %% "cats-core"                       % "2.10.0",
    "com.lucidchart"    %% "xtract"                          % "2.3.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatestplus" %% "scalatestplus-scalacheck"      % "3.1.0.0-RC2",
    "wolfendale"        %% "scalacheck-gen-regexp"         % "0.1.2",
    "uk.gov.hmrc"       %% s"bootstrap-test-$playVersion"  % bootstrap_ver,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-test-$playVersion" % mongo_ver,
    "org.mockito"       %% "mockito-scala"                 % "1.17.31"
  ).map(_ % Test)
}
