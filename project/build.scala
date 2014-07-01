import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object OlccsBuild extends Build {
  val Organization = "org.zorel"
  val Name = "Olccs"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.10.3"
  val ScalatraVersion = "2.2.2"

  lazy val project = Project (
    "olccs",
    file("."),
    settings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      resolvers += "Big Bee Consultants" at "http://repo.bigbeeconsultants.co.uk/repo",
      resolvers += "BoilerPipe" at "http://boilerpipe.googlecode.com/svn/repo",
      resolvers += "DB4O" at "http://source.db4o.com/maven/",
        libraryDependencies ++= Seq(
        "org.jsoup" % "jsoup" % "1.7.3",
        "com.netaporter" %% "scala-uri" % "0.4.1",
        "uk.co.bigbeeconsultants" %% "bee-client" % "0.26.6",
        "uk.co.bigbeeconsultants" %% "bee-config" % "1.1.+",
        "org.scalatra" %% "scalatra-json" % "2.2.2",
        "org.json4s"   %% "json4s-jackson" % "3.1.0",
        "org.eclipse.jetty" % "jetty-websocket" % "8.1.8.v20121106" % "container",
        "org.elasticsearch" % "elasticsearch" % "1.1.0",
        "com.sksamuel.elastic4s" %% "elastic4s" % "1.0.0.0",
        "com.db4o" % "db4o-full-java5" % "8.0-SNAPSHOT",
        "de.l3s.boilerpipe" % "boilerpipe" % "1.2.0",
        "net.sourceforge.nekohtml" % "nekohtml" % "1.9.20",
        "org.xhtmlrenderer" % "flying-saucer-core" % "9.0.6",
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "org.scalatra" %% "scalatra-auth" % ScalatraVersion,
        "com.github.nscala-time" %% "nscala-time" % "1.2.0",
        "org.scribe" % "scribe" % "1.3.5",
        "nl.grons" %% "metrics-scala" % "3.2.0_a2.3",
	      "com.codahale.metrics" % "metrics-graphite" % "3.0.2",
	      "com.codahale.metrics" % "metrics-jvm" % "3.0.2",
        "c3p0" % "c3p0" % "0.9.1.2",
        "com.typesafe.slick" %% "slick" % "1.0.1",
        "com.h2database" % "h2" % "1.3.166",
        "ch.qos.logback" % "logback-classic" % "1.0.13" % "runtime",
        "org.eclipse.jetty" % "jetty-webapp" % "8.1.10.v20130312" % "container",
        "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
    ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq.empty,  /* add extra bindings here */
            Some("templates")
          )
        )
      }
    )
  )
}
