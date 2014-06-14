package org.zorel.olccs.actors

import org.slf4j.LoggerFactory
import akka.actor.Actor
import org.zorel.olccs.models._
import javax.net.ssl._
import java.security.cert.X509Certificate
import uk.co.bigbeeconsultants.http._
import scala.Some
import java.security.SecureRandom
import uk.co.bigbeeconsultants.http.{HttpClient, Config}
import org.zorel.olccs.elasticsearch.ElasticSearch

class LinkActor extends Actor{

  val l = LoggerFactory.getLogger(getClass)

  def receive = {
    case a: Link => {
      // TODO: finir la partie
      /*a.process*/



    }
  }
}