import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "5.14.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % "0.55.0",
    "com.fasterxml.woodstox" % "woodstox-core" % "6.2.6",
    "net.java.dev.msv" % "msv" % "2013.5.1"               
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.14.0"            % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % "0.55.0"            % Test,
    "org.mockito"             %% "mockito-scala"              % "1.10.6"            % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8"            % "test, it",
    "org.scalatest"           %% "scalatest"                  % "3.1.0"             % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0"             % Test,
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current % Test
  )
}
