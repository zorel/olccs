
package org.zorel.olccs

import org.zorel.olccs.models.{Slip, CustomBoard, ConfiguredBoard}
import org.scalatra._
import org.slf4j.LoggerFactory
import javax.net.ssl.{KeyManager, SSLContext, TrustManager}
import java.security.SecureRandom
import uk.co.bigbeeconsultants.http._
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.header._
import uk.co.bigbeeconsultants.http.header.HeaderName._
import scala.Some
import uk.co.bigbeeconsultants.http.header.Cookie


class TribuneServlet extends OlccsStack {
  val l = LoggerFactory.getLogger(getClass)

  get("/:tribune/remote(.:ext)") {
    request.getHeader("X-Olccs-Private") match {
      case "1" => {
        val name = params("tribune")
        val get_url = params("getUrl")
        val post_url = params("postUrl")
        val post_parameter = params("postData")
        val slip = params("slip")
        val from = params.getOrElse("last", "0").toInt
        val b = CustomBoard(name, get_url, "last", Slip.Encoded, post_url, post_parameter)
        params.getOrElse("ext","xml") match {
          case "json" => {
            contentType = formats("json")
            Ok(b.backend_json)
          }
          case "xml" => {
            contentType = formats("xml")
            Ok(b.backend_xml)
          }
          case "tsv" => {
            contentType = formats("tsv")
            Ok(b.backend_tsv)
          }
        }
      }
      case _ => {
        val b = ConfiguredBoard.boards(params("tribune"))
        val from = params.getOrElse("last", "0").toInt
        val to:Option[Int] = params.get("to") match {
          case Some(t) => Some(t.toInt)
          case None => None
        }

        val size:Int = params.getOrElse("size","50").toInt
        params.getOrElse("ext","xml") match {
          case "json" => {
            contentType = formats("json")
            Ok(b.backend_json(from,to,size))
          }
          case "xml" => {
            contentType = formats("xml")
            Ok(b.backend_xml(from,to,size))
          }
          case "tsv" => {
            contentType = formats("tsv")
            Ok(b.backend_tsv(from,to,size))
          }
        }
      }

    }
  }

  get("/:tribune/search(.:ext)") {
    val b = ConfiguredBoard.boards(params("tribune"))
    val from = params.getOrElse("last", "0").toInt
    val query = params.getOrElse("query","")
    val to:Option[Int] = params.get("to") match {
      case Some(t) => Some(t.toInt)
      case None => None
    }

    val size:Int = params.getOrElse("size","50").toInt
    params.getOrElse("ext","xml") match {
      case "json" => {
        contentType = formats("json")
        Ok(b.search_json(query,from,to,size))
      }
      case "xml" => {
        contentType = formats("xml")
        Ok(b.search_xml(query,from,to,size))
      }
      case "tsv" => {
        contentType = formats("tsv")
        Ok(b.search_tsv(query,from,to,size))
      }
    }
  }

  get("/:tribune/reference-search(.:ext)") {
    val b = ConfiguredBoard.boards(params("tribune"))
    val from = params.getOrElse("from", Int.MaxValue.toString).toInt
    val res = b.post_from_horloge(params("timestamp"),from)
    res.length match {
      case 0 => NotFound("no response")
      case _ =>
        params.getOrElse("ext","xml") match {
          case "json" => {
            contentType = formats("json")
            Ok(res(0).to_s)
          }
          case "xml" => {
            contentType = formats("xml")
            Ok(res(0).to_xml)
          }
          case "tsv" => {
            contentType = formats("tsv")
            Ok(res(0).to_tsv)
          }
        }
    }
  }

  post("/:tribune/post(.:ext)") {
    val message = params.get("postdata") match {
      case Some(p) => p
      case None => params.get("message") match {
        case Some(m) => m
        case None => ""
      }
      case _ => ""
    }

    val cookies = params.get("cookie") match {
      case Some(c) => {
        c match {
          case "" => Map[String, String]()
          case _ => {
            val a: Array[String] = c.split("=")
            Map((a(0), a(1)))
          }
        }
      }
      case None => request.cookies
      case _ => Map[String, String]()
    }

    val user_agent = params.get("ua") match {
      case Some(u) => u
      case None => request.header("User-Agent").getOrElse("").toString

    }

    var xpostid = ""
    request.getHeader("X-Olccs-Private") match {
      case "1" => {
        val name = params("tribune")
        val get_url = ""
        val post_url = params("posturl")
        val post_data = params("postdata")
        val pos_equal = post_data.indexOf('=')
        val post_parameter = post_data.substring(0, pos_equal)
        val message = post_data.substring(pos_equal+1, post_data.length)
        val cb = CustomBoard(name, get_url, "last", Slip.Encoded, post_url, post_parameter)
        xpostid = cb.post(cookies.toMap, user_agent, message)
        response.setHeader("X-Post-Id", xpostid)
        Ok()
      }
      case _ => {
        // TODO: gestion de l'ua, pour l'instant ça mets l'ua présente dans le user-agent du post
        val b = ConfiguredBoard.boards(params("tribune"))
        xpostid = b.post(cookies.toMap, user_agent, message)
        response.setHeader("X-Post-Id", xpostid)
        params.get("last") match {
          case Some(l) => params.getOrElse("ext","xml") match {
            case "json" => {
              contentType = formats("json")
              Ok(b.backend_json(l.toInt))
            }
            case "xml" => {
              contentType = formats("xml")
              val pp = new scala.xml.PrettyPrinter(80, 2)
              Ok(pp.format(b.backend_xml(l.toInt)))
            }
            case "tsv" => {
              contentType = formats("tsv")
              Ok(b.backend_tsv(l.toInt))
            }
          }
          case None => Ok()
        }
      }
    }
  }

  post("/:tribune/login") {
    val b = ConfiguredBoard.boards(params("tribune"))

    val login = params("user")
    val password = params("password")

    val cookies = b.login(login,password)
    l.info(cookies.toString)

    cookies.map(c => response.addCookie(org.scalatra.Cookie(c._1, c._2)))

    Ok("ok")

  }
//  request.getHeader("X-Olccs-Private").toInt == 1

  post("/:tribune/post(.:ext)", params.getOrElse("private", "0") == "1") {
    val postdata = params("postdata")
    val ua = params("ua")
    val post_url = params("posturl")
    val pos = postdata.indexOf('=')
    val post_parameter = postdata take pos-1
    val post_value = postdata.drop(pos+1).replace("#{plus}#","+").
      replace("#{amp}#","&").
      replace("#{dcomma}#",";").
      replace("#{percent}#","%")

    val headers = Headers(
      USER_AGENT -> ua,
      REFERER -> post_url
    )

    val cookies = params.get("cookie") match {
      case Some(c) => {
        c match {
          case "" => Map[String, String]()
          case _ => {
            val a: Array[String] = c.split("=")
            Map((a(0), a(1)))
          }
        }
      }
      case None => request.cookies
      case _ => Map[String, String]()
    }

    val user_agent = params.get("ua") match {
      case Some(u) => u
      case None => request.header("User-Agent").getOrElse("").toString
    }
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

    val requestBody = RequestBody(Map(post_parameter -> post_value))
    val response_post = http_client.post(post_url, Some(requestBody), headers, cj)


    val xpostid = response_post.headers.get(HeaderName("X-Post-Id")) match {
      case Some(h) => h.toString()
      case None => ""
    }

    response.setHeader("X-Post-Id", xpostid)
  }



}
