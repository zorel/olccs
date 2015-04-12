package org.zorel.olccs

import java.io.File
import java.util.concurrent.TimeUnit
import java.lang.management.ManagementFactory
import java.net.InetSocketAddress
import com.codahale.metrics._
import com.codahale.metrics.graphite._
import com.codahale.metrics.jvm._
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.LoggerFactory
import org.zorel.olccs.models.{Storage, User}
import uk.co.bigbeeconsultants.bconfig.Config


import scala.slick.session.{Session, Database}

object OlccsConfig {
  val l = LoggerFactory.getLogger(getClass)

  val fallback = Config.fromClasspath("default.properties")
  val file = new File(System.getProperty("user.home")+"/.olccs/config.properties")

  val config = if (file.exists) {
    l.info("Loading config from " + file.getAbsolutePath)
    Config(file).verifyKeys(fallback.keySet)
  } else {
    l.info("Loading default config")
    fallback
  }

}
