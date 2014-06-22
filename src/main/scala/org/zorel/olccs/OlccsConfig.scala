package org.zorel.olccs

import java.io.File

import com.codahale.metrics.JmxReporter
import org.slf4j.LoggerFactory
import uk.co.bigbeeconsultants.bconfig.Config

object OlccsConfig {
  val l = LoggerFactory.getLogger(getClass)
  /** The application wide metrics registry. */
  val metricRegistry = new com.codahale.metrics.MetricRegistry()
  val fallback = Config.fromClasspath("default.properties")
  val file = new File(System.getProperty("user.home")+"/.olccs/config.properties")
  val config = if (file.exists) {
    l.info("Loading config from " + file.getAbsolutePath)
    Config(file).verifyKeys(fallback.keySet)
  } else {
    l.info("Loading default config")
    fallback
  }

  val reporter = JmxReporter.forRegistry(metricRegistry).build();
  reporter.start();
}



trait Instrumented extends nl.grons.metrics.scala.InstrumentedBuilder {
  val metricRegistry = OlccsConfig.metricRegistry
}

