package org.zorel.olccs.actors

import _root_.akka.actor.Actor
import org.slf4j.LoggerFactory
import org.zorel.olccs.models.{ConfiguredBoard, Board}

class ReloadWorker extends Actor {
  val l = LoggerFactory.getLogger(getClass)

  def receive = {
    case b: ConfiguredBoard => b.index
  }

}
