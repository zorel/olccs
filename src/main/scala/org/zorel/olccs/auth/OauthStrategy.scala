package org.zorel.olccs.auth

/**
 * Created by zorel on 2/25/14.
 */
import org.slf4j.LoggerFactory
import org.zorel.olccs.OlccsConfig
import org.zorel.olccs.models._
import org.scribe.builder._
import javax.servlet.http.{HttpSession, HttpServletResponse, HttpServletRequest}
import scala.slick.driver.H2Driver.simple._
import scala.collection.JavaConversions._
import org.scalatra.auth.ScentryStrategy
import org.scalatra._


object OauthStrategy {

//  val NETWORK_NAME = "Google"
//  val AUTHORIZE_URL = "https://www.google.com/accounts/OAuthAuthorizeToken?oauth_token="
//  val SCOPE = "https://docs.google.com/feeds/"

  val service = new ServiceBuilder()
    .provider(classOf[LinuxfrAPI])
    .apiKey(OlccsConfig.config("api_key"))
    .apiSecret(OlccsConfig.config("api_secret"))
    .callback(OlccsConfig.config("api_callback"))
    .build()

  def getRequestTokenUrl = {
    service.getAuthorizationUrl(null)
//    AUTHORIZE_URL + requestToken.getToken()
  }

}

class OurOAuthStrategy(protected val app: ScalatraBase)
                      (implicit request: HttpServletRequest, response: HttpServletResponse)
      extends ScentryStrategy[U] {
  val l = LoggerFactory.getLogger(getClass)
  val user_name = app.session("user_name").asInstanceOf[String]

  private def remoteAddress = {
    val proxied = app.request.getHeader("X-FORWARDED-FOR")
    val res = if (proxied != "") proxied else app.request.getRemoteAddr

    res
  }

  def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[U] = {

    if (user_name == "")
      return None

    val user = User.byLogin(user_name)

    if (user == None) {
      OlccsConfig.db withSession { implicit session: scala.slick.session.Session =>
        User.insert(U(None, user_name, "", User.gen_token(user_name)))
      }
      val ret = User.byLogin(user_name)
      return ret
    } else {
      return user
    }
    //    val ret = user match {
    //      case Some(u) => Some(u)
    //      case None => {
    //        OlccsConfig.db withSession { implicit session: Session =>
    //          val u = U(app.session("user_name").asInstanceOf[String], "", User.gen_token)
    //          User.insert(u)
    //        }
    //        Some(User.byLogin("user_name"))
    //      }


  //    l.debug(ret.get.name)
  }

  /**
   * Clears the remember-me cookie for the specified user.
   */
  override def beforeLogout(u: U)(implicit request: HttpServletRequest, response: HttpServletResponse) = {

    app.session.invalidate()

  }

}