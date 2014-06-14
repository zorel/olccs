package org.zorel.olccs.ssl

import javax.net.ssl.{SSLSession, HostnameVerifier}

/** Don't use this in production code!!! */
class DumbHostnameVerifier extends HostnameVerifier {
  def verify(p1: String, p2: SSLSession) = true
}
