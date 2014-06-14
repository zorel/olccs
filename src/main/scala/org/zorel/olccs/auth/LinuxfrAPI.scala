package org.zorel.olccs.auth

import org.scribe.model.{Verb, OAuthConfig, Token}
import org.scribe.builder.api.DefaultApi20
import org.scribe.utils.OAuthEncoder
import org.scribe.extractors.{JsonTokenExtractor, AccessTokenExtractor}

/**
 * Created by zorel on 5/28/14.
 */
class LinuxfrAPI extends DefaultApi20 {
  val authorize_url = "linuxfr.org/auth/oauth/authorize?client_id=%s&redirect_uri=%s"
  val access_token_resource = "linuxfr.org/auth/oauth/access_token"

  override def getAuthorizationUrl(config: OAuthConfig):String = {
    String.format("http://"+authorize_url, config.getApiKey, OAuthEncoder.encode(config.getCallback))
  }

  override def getAccessTokenEndpoint:String = {
    "http://" + access_token_resource
  }

  override def getAccessTokenVerb:Verb = {
    Verb.POST
  }

  override def getAccessTokenExtractor: AccessTokenExtractor = {
    new JsonTokenExtractor
  }

}
