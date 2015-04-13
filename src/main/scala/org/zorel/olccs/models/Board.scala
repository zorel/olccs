package org.zorel.olccs.models


import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl._

import com.netaporter.uri.dsl._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Entities.EscapeMode
import org.slf4j.LoggerFactory
import org.zorel.olccs.ssl
import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.header.HeaderName._
import uk.co.bigbeeconsultants.http.header.{Cookie, _}
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.response._

import scala.collection.immutable.Map
import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, XML}



abstract class Board(val name: String,
             val get_url: String,
             val lastid_parameter: String,
             val slip_type: Slip.Value,
             val post_url: String,
             val post_parameter: String) {

  val l = LoggerFactory.getLogger(getClass)
  val lock: AtomicBoolean = new AtomicBoolean(false)
  var lastid=0L

  def backend_orig: Elem = {
//    if(lock.get() == true) {
//      l.error("Fetch request already in progress for %s".format(name))
//    }
    val f = javax.xml.parsers.SAXParserFactory.newInstance()
    f.setValidating(false)
    f.setFeature("http://xml.org/sax/features/validation", false)
    f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
    f.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
    f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    val p = f.newSAXParser()

    // Create a new trust manager that trusts all certificates
    val trustAllCerts = Array[TrustManager](new ssl.DumbTrustManager)

    // Activate the new trust manager
    val sc = SSLContext.getInstance("SSL")
    sc.init(Array[KeyManager](), trustAllCerts, new SecureRandom())

    val config = Config(followRedirects = false,
      keepAlive = false,
      userAgentString = Some("olccs-scala"),
      sslSocketFactory = Some(sc.getSocketFactory),
      hostnameVerifier = Some(new ssl.DumbHostnameVerifier))
    val httpClient = new HttpClient(config)

    val uri = lastid_parameter match {
      case "" => get_url
      case _ => get_url ? (lastid_parameter -> lastid)
    }
//    ?
    val response = Try(httpClient.get(uri.toString))

    val r = response match {
      case Failure(thrown) => {
        l.error("Error fetching " + name + " : " + thrown.getMessage)
      }
      case Success(s) => {
        s.status match {
          case Status.S304_NotModified => return <board></board>
          case _ => s
        }
      }
    }

    try {
      XML.withSAXParser(p).loadString(response.get.body.asString)
    } catch {
      case ex : Throwable =>
        l.error("Oops in backend reload for " + name)
        l.debug(ex.getStackTrace.mkString("\n"))

        //        l.error(response.body.asString)
        //XML.withSAXParser(p).loadString()
        <board></board>
    }
  }


  def sanitized_backend: List[Post] = {
    val b = backend_orig
    (b \ "post" filter( x => (x \ "@id").text.toInt > lastid)).toList.reverseMap { p =>
      l.debug("last: " + lastid + "=> " + (p \ "@id").text)
      val m = slip_type match {
        case Slip.Encoded => "<message>" + (p \ "message").text.replaceAll("""(?m)\s+""", " ") + "</message>"
        case Slip.Raw => (p \ "message").toString.replaceAll("""(?m)\s+""", " ")
      }
      var post: Post = null
      //      try {

      // Parse str into a Document
      val doc : Document = Jsoup.parseBodyFragment(m.replaceAll("""(?m)\s+""", " ").replaceAll("\\p{Cntrl}", ""))

      // Clean the document.
      //      val doc2 = new Cleaner(Whitelist.simpleText()).clean(doc)

      // Adjust escape mode
      doc.outputSettings().escapeMode(EscapeMode.xhtml)
      doc.outputSettings().prettyPrint(false)

      // Get back the string of the body.
      //      l.info(doc.body().html())
      lastid = (p \ "@id").text.toInt
      Post(name,
        (p \ "@id").text,
        (p \ "@time").text,
        (p \ "info").text.replaceAll("""(?m)\s+""", " ").replaceAll("\\p{Cntrl}", ""),
        (p \ "login").text.replaceAll("""(?m)\s+""", " ").replaceAll("\\p{Cntrl}", ""),
        XML.loadString(doc.body().html())
      )
      //      } catch {
      //        case ex: Throwable => {
      //          l.info(m)
      //          l.info("to string:" + (p \ "message").toString())
      //          l.info("text: " + (p \ "message").text)
      //        }
      //      }
    }
  }


  // From: initial (eq. to last)
  // To:
  // Size: maximum size of backend, in number of posts
  def backend(from:Int, to:Option[Int], size:Int): List[Post]

  def backend_json(from:Int=0, to:Option[Int]=None, size:Int=50): String = {

    compact(render(("board" ->
      ("site" -> name)) ~
      ("posts" -> backend(from,to,size).map { p =>
          ("id" -> p.id) ~
          ("time" -> p.time) ~
          ("info" -> p.info) ~
          ("login" -> p.login) ~
          ("message" -> p.message)
      })))
  }

  def backend_xml(from:Int=0, to:Option[Int]=None, size:Int=50): Elem = {
    <board site={name}>
      {
      backend(from,to,size).map { p =>
        p.to_xml
      }
    }
    </board>
  }

  def backend_tsv(from:Int=0, to:Option[Int]=None, size:Int=50): String = {
//    "board\tid\ttime\tinfo\tlogin\tmessage\n" +
    backend(from,to,size).reverseMap(_.to_tsv).mkString("")
  }

  def post(cookies: Map[String, String], ua: String, content: String): String = {
    val c = content.replace("#{plus}#","+").
      replace("#{amp}#","&").
      replace("#{dcomma}#",";").
      replace("#{percent}#","%")

    val headers = Headers(
      USER_AGENT -> ua,
      REFERER -> post_url
    )

    // Create a new trust manager that trusts all certificates
    val trustAllCerts = Array[TrustManager](new ssl.DumbTrustManager)

    // Activate the new trust manager
    val sc = SSLContext.getInstance("SSL")
    sc.init(Array[KeyManager](), trustAllCerts, new SecureRandom())

    val config = Config(followRedirects = false,
      keepAlive = false,
      userAgentString = Some("olccs-scala"),
      sslSocketFactory = Some(sc.getSocketFactory),
      hostnameVerifier = Some(new ssl.DumbHostnameVerifier))

    val domain = new java.net.URL(post_url).getHost
    val cj: CookieJar = CookieJar(cookies.map(x => Cookie(x._1,x._2, domain)).toList)

    val http_client = new HttpClient(config)

    l.debug("post_parameter => %s".format(post_parameter))
    val requestBody = RequestBody(Map(post_parameter -> c))
    val response = http_client.post(post_url, Some(requestBody), headers, cj)

    response.headers.get(HeaderName("X-Post-Id")) match {
      case Some(h) => h.toString()
      case None => ""
    }

//    println(r.status)

    //l.info(cookies.toString())
  }
}

