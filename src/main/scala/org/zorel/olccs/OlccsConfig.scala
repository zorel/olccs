package org.zorel.olccs

import java.io.File

import org.slf4j.LoggerFactory
import uk.co.bigbeeconsultants.bconfig.Config

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
