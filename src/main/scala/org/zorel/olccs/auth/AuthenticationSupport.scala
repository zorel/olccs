package org.zorel.olccs.auth

/**
 * Created by zorel on 2/25/14.
 */
import org.scalatra.auth.{ScentrySupport, ScentryConfig}
import org.scalatra.{FlashMapSupport, ScalatraBase}
import org.zorel.olccs.models.User


trait AuthenticationSupport extends ScentrySupport[User] {
  self: ScalatraBase =>

  val realm = "Scalatra Basic Auth Example"

  protected def fromSession = { case id: String => User.byId(id) }
  protected def toSession = { case usr: User => usr.id }

  protected val scentryConfig = (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]


  override protected def configureScentry = {
    scentry.unauthenticated {
      scentry.strategies("OAuth").unauthenticated()
    }
  }

  override protected def registerAuthStrategies = {
    scentry.register("OAuth", app => new OurOAuthStrategy(app))
  }



}