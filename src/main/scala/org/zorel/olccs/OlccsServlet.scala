package org.zorel.olccs

import org.slf4j.LoggerFactory
import org.zorel.olccs.models.{ConfiguredBoard, Board}
import javax.net.ssl._
import org.zorel.olccs.ssl._
import java.security.SecureRandom
import uk.co.bigbeeconsultants.http._
import scala.util.parsing.json.JSON
import org.scalatra.{Ok, BadRequest}

class OlccsServlet extends OlccsStack {

  val l = LoggerFactory.getLogger(getClass)

  get("/") {
    <html>plop</html>
  }

  get("/totoz") {
    params.get("url") match {
      case Some(u) => {
        contentType = "application/xml"
        val search_url = u.replace("{question}","?").replace(" ", "+")
        // Create a new trust manager that trusts all certificates
        val trustAllCerts = Array[TrustManager](new DumbTrustManager)

        // Activate the new trust manager
        val sc = SSLContext.getInstance("SSL")
        sc.init(Array[KeyManager](), trustAllCerts, new SecureRandom())

        val config = Config(followRedirects = false,
          keepAlive = false,
          userAgentString = Some("olccs-scala"),
          sslSocketFactory = Some(sc.getSocketFactory),
          hostnameVerifier = Some(new DumbHostnameVerifier))
        val httpClient = new HttpClient(config)
        httpClient.get(search_url).body.asString
      }
      case None => ""
    }
  }

  get("/boards_config.js") {
    contentType = "text/javascript"
    ConfiguredBoard.boards.map { case (name, b) =>
      val name = b.name
      val cookie = b.cookie_name
      val color = b.color
      val aliases = b.aliases

      s"""
var $name = new Board('$name', false);
$name.getUrl = 'ga';
$name.postUrl = 'bu';
$name.cookie = '$cookie';
$name.color = '$color';
$name.aliases = '$aliases';
GlobalBoards['$name'] = $name;
"""
    }.mkString("\n")
  }

  get("/boards.xml") {
    contentType = "application/xml"
    <sites>{
      ConfiguredBoard.boards.map( b =>
        <site name={b._2.name}>
          <module name="board" title="tribune" type="application/board+xml">
            <backend path={b._2.get_url} public="true" refresh="15" tags_encoded="false"/>
            <post anonymous="true" max_length="512" method="post" path={b._2.post_url}>
              <field name={b._2.post_parameter}/>
            </post>
            <login method="post" path={b._2.login_url}>
              <username name={b._2.login_parameter}/>
              <password name="pass"/>
              <remember name=""/>
              <cookie name=""/>
            </login>
          </module>
        </site>
      )}
    </sites>
  }

  get("/multiremote(.:ext)") {
    params.get("boards") match {
      case Some(b) => {
        JSON.parseFull(b) match {
          case Some(map: Map[String, Double]) => {
            val size:Int = params.getOrElse("size","250").toInt

            params.getOrElse("ext","xml") match {
              case "json" => {
                contentType = "application/json"
                val t = for((k,v) <- map) yield ConfiguredBoard.boards(k).backend_json(v.toLong,None,size)
                Ok("[" + t.mkString(",") + "]")
              }
            case "xml" => {
              contentType = "application/xml"
              val t = for((k,v) <- map) yield ConfiguredBoard.boards(k).backend_xml(v.toLong,None,size)
              Ok("<boards>" + t.mkString("") + "</boards>")
              }
              case "tsv" => {
                contentType = "text/tsv"
                val t = for((k,v) <- map) yield ConfiguredBoard.boards(k).backend_tsv(v.toLong,None,size)
                Ok(t.mkString(""))
              }
            }
          }
          case None => BadRequest("Check input format")
          case other => BadRequest("Check input format")
        }
      }
      case _ => BadRequest("Check input format")
    }
  }
}

