package org.zorel.olccs.actors

import _root_.akka.actor.{Props, Actor}
import org.slf4j.LoggerFactory
import org.zorel.olccs.models.{ConfiguredBoard, Board}
import _root_.akka.routing.RoundRobinRouter


class ReloadActor extends Actor {
  val l = LoggerFactory.getLogger(getClass)
  val worker = context.actorOf(Props[ReloadWorker].withRouter(RoundRobinRouter(10)), name = "Reloadworker")

  def receive = {
    case "refresh" => {
      l.debug("Receive refresh order")
      for (b@(s, board) <- ConfiguredBoard.boards) {
        l.debug("Refresh for %s" format s)
        worker ! board
      }

    }
  }

}
