package org.zorel.olccs

import java.io.File
import java.util.concurrent.TimeUnit
import java.lang.management.ManagementFactory
import java.net.InetSocketAddress
import com.codahale.metrics._
import com.codahale.metrics.graphite._
import com.codahale.metrics.jvm._
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


  metricRegistry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()))
  metricRegistry.register("jvm.gc", new GarbageCollectorMetricSet())
  metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet())
  metricRegistry.register("jvm.threads", new ThreadStatesGaugeSet())

  val graphite: Graphite = new Graphite(new InetSocketAddress("127.0.0.1", 2003))

  def graphiteReporter = GraphiteReporter.forRegistry(metricRegistry)
                                             .prefixedWith("olccs.halifirien.info")
                                             .convertRatesTo(TimeUnit.SECONDS)
                                             .convertDurationsTo(TimeUnit.MILLISECONDS)
                                             .filter(MetricFilter.ALL)
                                             .build(graphite)
}



trait Instrumented extends nl.grons.metrics.scala.InstrumentedBuilder {
  val metricRegistry = OlccsConfig.metricRegistry
}

