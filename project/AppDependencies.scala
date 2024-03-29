import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {
  val bootstrap_ver = "7.22.0"
  val mongo_ver = "1.3.0"
  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % bootstrap_ver,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"        % mongo_ver,
    "org.julienrf"      %% "play-json-derived-codecs"  % "10.1.0",
    "org.typelevel"     %% "cats-core"                 % "2.9.0",
    "com.lucidchart"    %% "xtract"                    % "2.3.0-alpha3"
  )

  val test = Seq(
    "org.scalatestplus"      %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "wolfendale"             %% "scalacheck-gen-regexp"    % "0.1.2",
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"   % bootstrap_ver,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"  % mongo_ver,
    "org.mockito"            %% "mockito-scala"            % "1.17.12",
    "com.vladsch.flexmark"    % "flexmark-all"             % "0.64.6",
    "org.scalatest"          %% "scalatest"                % "3.2.15",
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0",
    "com.github.tomakehurst"  % "wiremock-standalone"      % "2.27.2",
    "com.typesafe.play"      %% "play-test"                % PlayVersion.current
  ).map(_ % "test, it")
}
