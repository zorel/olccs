package org.zorel.olccs.models

import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable.Map
import scala.xml.Elem

class CustomBoard(name: String,
             get_url: String,
             lastid_parameter: String,
             slip_type: Slip.Value,
             post_url: String,
             post_parameter: String) extends Board(name, get_url, lastid_parameter, slip_type, post_url, post_parameter) {

  def backend(from: Int, to: Option[Int], size: Int): List[Post] = {
    backend
  }

  def backend: List[Post] = {
    sanitized_backend.reverse
//    val b = backend_orig
//    slip_type match {
//      case Slip.Encoded => l.error("+++++++++++ encoded")
//      case Slip.Raw => l.error("========== raw")
//    }
//    val t = for(p <- (b \ "post").reverse) yield {
//      val m = slip_type match {
//        case Slip.Encoded => "<message>" + (p \ "message").text + "</message>"
//        case Slip.Raw => (p \ "message").toString
//      }
//      Post(name, (p \ "@id").text, (p \ "@time").text, (p \ "info").text, (p \ "login").text, m)
//    }
//    l.info("t.size " + t.size)
//    t.toList
  }

  def backend_json: String = {

    compact(render(("board" ->
      ("site" -> name)) ~
      ("posts" -> backend.map { p =>
          ("id" -> p.id) ~
            ("time" -> p.time) ~
            ("info" -> p.info) ~
            ("login" -> p.login) ~
            ("message" -> p.message)
      })))
  }

  def backend_xml: Elem = {
    <board site={name}>
      {
      backend.map { p =>
        p.to_xml
      }
    }
    </board>
  }

  def backend_tsv: String = {
    "id\ttime\tinfo\tlogin\tmessage\n" + backend.reverseMap(_.to_tsv).mkString("")
  }

  override def post(cookies: Map[String, String], ua: String, content: String): String = {
    super.post(cookies, ua, content)
  }
}

object CustomBoard {
//  var boards: ArrayBuffer[Board] = new ArrayBuffer[Board]()
  var boards = new scala.collection.mutable.HashMap[String, Board]()
  def apply(name: String,
    get_url: String,
    lastid_parameter: String,
    slip_type: Slip.Value,
    post_url: String,
    post_parameter: String) = {
      new CustomBoard(name, get_url, lastid_parameter, slip_type, post_url, post_parameter)

    }
}

