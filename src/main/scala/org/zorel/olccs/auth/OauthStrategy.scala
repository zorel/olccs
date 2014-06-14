package org.zorel.olccs.auth

/**
 * Created by zorel on 2/25/14.
 */
import java.util._

import org.zorel.olccs.models.User
import org.scribe.builder._
import org.scribe.builder.api._
import org.scribe.builder.api.TwitterApi
import org.scribe.model._
import org.scribe.oauth._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

//import net.liftweb.common.{Box, Empty, Failure, Full}
import org.scalatra.auth.ScentryStrategy
import org.slf4j.LoggerFactory
import org.scalatra._


object OauthStrategy {

//  val NETWORK_NAME = "Google"
//  val AUTHORIZE_URL = "https://www.google.com/accounts/OAuthAuthorizeToken?oauth_token="
//  val SCOPE = "https://docs.google.com/feeds/"

  val service = new ServiceBuilder()
    .provider(classOf[LinuxfrAPI])
    .apiKey("03147c927e15285b9a8243060803375f")
    .apiSecret("ede976044b35eaee688d794799e943a2")
    .callback("http://localhost:8080/u/oauth2callback")
    .build()

  def getRequestTokenUrl(requestToken: Token)={
//    oauth_token=requestToken.getToken()
    service.getAuthorizationUrl(null)
//    AUTHORIZE_URL + requestToken.getToken()
  }

}

class OurOAuthStrategy(protected val app: ScalatraBase) extends ScentryStrategy[User]
{

  val COOKIE_KEY = "rememberMe"

  private def remoteAddress = {
    val proxied = app.request.getHeader("X-FORWARDED-FOR")
    val res = if (proxied != "" ) proxied else app.request.getRemoteAddr

    res
  }

  /**
   * Authenticates a user by validating the username (or email) and password request params.
   */
  def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[User] = {
    var ret: Option[User] = None

    println("param: "+app.params("oauth_token") +" save t: " + app.session("oauth_token"))
    if(app.params("oauth_token") == app.session("oauth_token")){
      ret = Some(User.byId("1"))
    }


    ret
  }

  /**
   * Clears the remember-me cookie for the specified user.
   */
  override def beforeLogout(u: User)(implicit request: HttpServletRequest, response: HttpServletResponse) = {

    app.session.invalidate()

    app.cookies.get(COOKIE_KEY) foreach {
      _ => app.cookies.update(COOKIE_KEY, null)
    }
  }

}