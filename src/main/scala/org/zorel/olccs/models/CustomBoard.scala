package org.zorel.olccs.models

import org.slf4j.LoggerFactory
import scala.xml.{Elem, XML}
import org.json4s.JsonDSL._
import org.zorel.olccs.elasticsearch._
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.SearchHit
import org.json4s.jackson.JsonMethods._
import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.header._
import scala.collection.immutable.Map
import org.elasticsearch.index.query.{FilterBuilders, QueryBuilders}
import header.HeaderName._
import org.jsoup.Jsoup
import scala.Some
import uk.co.bigbeeconsultants.http.header.Cookie
import javax.net.ssl._
import java.security.cert.X509Certificate
import java.security.SecureRandom
import org.zorel.olccs.ssl

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

    compact(render((("board" ->
      ("site" -> name)) ~
      ("posts" -> backend.map { p =>
        (
          ("id" -> p.id) ~
            ("time" -> p.time) ~
            ("info" -> p.info) ~
            ("login" -> p.login) ~
            ("message" -> p.message)
          )
      }))))
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
    "id\ttime\tinfo\tlogin\tmessage\n" + backend.reverse.map(_.to_tsv).mkString("")
  }

  override def post(cookies: Map[String, String], ua: String, content: String): String = {
    super.post(cookies, ua, content)
  }

//  def login(login: String, password:String): Map[String,String] = {
//    val config = Config(followRedirects = false, keepAlive = false)
//    val http_client = new HttpClient(config)
//    val cj = CookieJar()
//    l.info("" + login + " " + password)
//    val requestBody = RequestBody(Map(login_parameter -> login,password_parameter -> password, "form_id" -> "user_login_block"))
//    val cookies = http_client.post(login_url, Some(requestBody), Headers(), cj).cookies
//    l.info(cookies.toString)
//    cookies match {
//      case Some(cj) => cj.cookies.map(c => (c.name, c.value)).toMap
//      case None => Map()
//    }
//  }
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

