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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Entities.EscapeMode
import scala.Some
import javax.net.ssl._
import java.security.SecureRandom
import org.zorel.olccs.ssl

class ConfiguredBoard(name: String,
             get_url: String,
             lastid_parameter: String,
             slip_type: Slip.Value,
             post_url: String,
             post_parameter: String,
             val login_url: String,
             val cookie_name: String,
             val login_parameter: String,
             val password_parameter: String) extends Board(name, get_url, lastid_parameter, slip_type, post_url, post_parameter) {

  lastid = {
    try {
      backend(0,None, 1)(0).id
    } catch {
      case _:Throwable => 0
    }
  }


// TODO
//  val raw = <post><message>test &amp; <b><u><i>meuh</i></u></b></message></post>
//  (raw \ "message").toString
//  (raw \ "message").text
//
//  val cdata = <post><message><![CDATA[test &amp; <b><u><i>meuh</i></u></b>]]></message></post>
//  (cdata \ "message").toString
//  (cdata \ "message").text
//
//  val encoded = <post><message>test &amp;#38; &lt;b&gt;&lt;i&gt;&lt;u&gt;meuh&lt;/u&gt;&lt;/i&gt;&lt;/b&gt;</message></post>
//  (encoded \ "message").toString
//  (encoded \ "message").text


  def index {
    l.debug("Start: index tribune %s" format name)
    val b = backend_orig
    (b \ "post" filter( x => (x \ "@id").text.toInt > lastid)).reverse.foreach { p =>
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
        post = Post(name,
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

      ElasticSearch.index(name, post)
    }
    l.debug("End: index tribune %s" format name)
  }

  // From: initial (eq. to last)
  // To:
  // Size: maximum size of backend, in number of posts
  def backend(from:Int=0, to:Option[Int]=None, size:Int=50): List[Post] = {
    l.debug("Entering backend for %s" format name)
//    l.info("==> %s %s %s".format(from, to, size))
    val q = to match {
      case Some(n) => QueryBuilders.rangeQuery("id").from(from).to(n)
      case None => QueryBuilders.rangeQuery("id").from(from)
    }

    val response: SearchResponse = ElasticSearch.query(name, q, size)
    val t = for (h: SearchHit <- response.getHits().hits()) yield {
      val id = h.field("id").getValue[Int]
      val time = h.field("time").getValue[String]
      val info = h.field("info").getValue[String]
      val login = h.field("login").getValue[String]
      val message = h.field("message").getValue[String]
      Post(name, id, time, info, login, message)
    }
    t.toList
  }

  def search(query:String, from:Int=0, to:Option[Int]=None, size:Int=50): List[Post] = {
    l.debug("Entering search for %s".format(name))
    val q = QueryBuilders.queryString(query)

    val response: SearchResponse = ElasticSearch.query(name, q, size)
    val t = for (h: SearchHit <- response.getHits().hits()) yield {
      val id = h.field("id").getValue[Int]
      val time = h.field("time").getValue[String]
      val info = h.field("info").getValue[String]
      val login = h.field("login").getValue[String]
      val message = h.field("message").getValue[String]
      Post(name, id, time, info, login, message)
    }
    t.toList
  }

  def search_json(q:String, from:Int=0, to:Option[Int]=None, size:Int=50): String = {

    compact(render((("board" ->
      ("site" -> name)) ~
      ("posts" -> search(q,from,to,size).map { p =>
        (("id" -> p.id))}))))
  }

  def search_xml(q:String, from:Int=0, to:Option[Int]=None, size:Int=50): Elem = {
    <board site={name}>
      {
      search(q,from,to,size).map { p =>
        p.to_xml
      }
      }
    </board>
  }

  def search_tsv(q:String, from:Int=0, to:Option[Int]=None, size:Int=50): String = {
    "id\ttime\tinfo\tlogin\tmessage\n" + search(q,from,to,size).reverse.map(_.to_tsv).mkString("")
  }

  override def post(cookies: Map[String, String], ua: String, content: String): String = {
    val ret = super.post(cookies, ua, content)
    index
    ret
  }

  // From: initial (eq. to last)
  // To:
  // Size: maximum size of backend, in number of posts
  def post_from_horloge(horloge:String, from:Int = Int.MaxValue): List[Post] = {
    l.debug("Entering post_from_horloge for %s" format name)
    //    l.info("==> %s %s %s".format(from, to, size))
    val hours = horloge.substring(0,2)
    val minutes = horloge.substring(2,4)
    val seconds = horloge.substring(4,6)
    val filter = "doc['time'].date.HourOfDay==%d and doc['time'].date.minuteOfHour==%d and  doc['time'].date.SecondOfMinute==%d".format(Integer.parseInt(hours),Integer.parseInt(minutes),Integer.parseInt(seconds))
    val q = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.scriptFilter(filter))

    val response: SearchResponse = ElasticSearch.query(name, q, 1)
    val t = for (h: SearchHit <- response.getHits.hits) yield {
      val id = h.field("id").getValue[Int]
      val time = h.field("time").getValue[String]
      val info = h.field("info").getValue[String]
      val login = h.field("login").getValue[String]
      val message = h.field("message").getValue[String]
      Post(name, id, time, info, login, message)
    }
    l.info("post_from_horloge %s".format(filter))
    t.toList
  }

  def login(login: String, password:String): Map[String,String] = {
    // Create a new trust manager that trusts all certificates
    val trustAllCerts = Array[TrustManager](new ssl.DumbTrustManager)

    // Activate the new trust manager
    val sc = SSLContext.getInstance("SSL")
    sc.init(Array[KeyManager](), trustAllCerts, new SecureRandom())
    val config = Config(followRedirects = false,
      keepAlive = false,
      sslSocketFactory = Some(sc.getSocketFactory),
      hostnameVerifier = Some(new ssl.DumbHostnameVerifier))

    val http_client = new HttpClient(config)
    val cj = CookieJar()

    val requestBody = RequestBody(Map(login_parameter -> login,password_parameter -> password, "form_id" -> "user_login_block"))
    val cookies = http_client.post(login_url, Some(requestBody), Headers(), cj).cookies

    cookies match {
      case Some(cj) => cj.cookies.map(c => (c.name, c.value)).toMap
      case None => Map()
    }
  }

  def stats: String = {
    ElasticSearch.tribune_stats(name).toString
  }
}

object ConfiguredBoard {
//  var boards: ArrayBuffer[Board] = new ArrayBuffer[Board]()
  var boards = new scala.collection.mutable.HashMap[String, ConfiguredBoard]()
  def apply(name: String,
    get_url: String,
    lastid_parameter: String,
    slip_type: Slip.Value,
    post_url: String,
    post_parameter: String,
    login_url: String,
    cookie_name: String,
    login_parameter: String,
    password_parameter: String) = {
      val b = new ConfiguredBoard(name, get_url, lastid_parameter, slip_type, post_url, post_parameter, login_url, cookie_name, login_parameter, password_parameter)
      boards += (name -> b)
      b
    }
}