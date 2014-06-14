package org.zorel.olccs

import org.slf4j.LoggerFactory
import org.zorel.olccs.auth.OauthStrategy
import org.scribe.model.{Token, Verifier, Verb, OAuthRequest}
import org.scalatra.DefaultValues
import scala.util.parsing.json.JSON

/**
 * Created by zorel on 5/12/14.
 */
class UserServlet extends OlccsStack {
  val l = LoggerFactory.getLogger(getClass)

  def redirectIfNotAuthenticated = {
    //scentry.authenticate('RememberMe, 'UserPassword)

    if (!isAuthenticated){
      redirect(url("/login"))
    }
  }

  def redirectIfAuthenticated = {
    if (isAuthenticated){
      redirect(url("/"))
    }
  }


  get("/") {
    redirectIfNotAuthenticated

    <html>
      <body>
        <h1>You logged in with oAuth</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
  }

  get("/login"){
    val tUrl = OauthStrategy.getRequestTokenUrl(null)

//    session("oauth_token") = OauthStrategy.oauth_token
    println(tUrl)

//    println(session)
//    println(session("oauth_token"))

    redirectIfAuthenticated

    contentType="text/html"

    "<html>" +
      "<body>" +
      "<a href=\""+tUrl+"\">Login</a>" +
      "</body>" +
      "</html>"
  }

  get("/logout"){

  }

  get("/oauth2callback"){
    println(params)
    val t = OauthStrategy.service.getAccessToken(null, new Verifier(params("code")))
    println(t)

    val r = new OAuthRequest(Verb.GET, "http://linuxfr.org/auth/oauth/user")

//    OauthStrategy.service.signRequest(t, r)
    r.addQuerystringParameter("bearer_token", t.getToken)

    val resp = r.send
    val infos = JSON.parseFull(resp.getBody).get.asInstanceOf[Map[String, String]]
    val login = infos.get("login").get.asInstanceOf[String]

    println(login)

    scentry.authenticate()

    if (isAuthenticated) {
      println("**SUCCESS**")
      redirectIfAuthenticated
    }else{
      println("FAILED")

    }

    redirectIfNotAuthenticated
  }

}
