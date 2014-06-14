package org.zorel.olccs.ssl

import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

/** Don't use this in production code!!! */
class DumbTrustManager extends X509TrustManager {
  def getAcceptedIssuers: Array[java.security.cert.X509Certificate] = null
  def checkClientTrusted(certs: Array[X509Certificate], authType: String) {}
  def checkServerTrusted(certs: Array[X509Certificate], authType: String) {}
}
