package org.zorel.olccs.models

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.zorel.olccs.OlccsConfig

import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession

case class U(id: Option[Long], name: String, md5: String, token: String)

object User extends Table[U]("user") {
  val db = OlccsConfig.db
  def id = column[Long]("id", O.AutoInc)
  def name = column[String]("name", O.NotNull)
  def md5 = column[String]("md5")
  def token = column[String]("token")

  def * = id.? ~ name ~ md5 ~ token <> (U.apply _, U.unapply _)

  def idx = index("idx_name", name, unique = true)

  def byLogin(l: String): Option[U] = db withSession {
    Query(User).filter(_.name === l).list.headOption
  }

//  // auto increment handler
//  def autoInc = * returning name
//
//  def insert(u: U) = db withSession {
//    autoInc.insert(u)
//  }
//
//  def update(name: String, u: U) = db withSession {
//    User.where(_.name === name).update(u)
//  }
//
//  def delete(name: String) = db withSession {
//    User.where(_.name === name).delete
//  }

  def gen_token(l: String) = {
    val HEX_DIGITS = List("0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f")
    val md = java.security.MessageDigest.getInstance("SHA-1")
    val salt = Math.random().toString
    val secret = OlccsConfig.config("secret_key")
    val digest = md.digest((salt + secret + l).getBytes)
    digest.map { (b:Byte) =>
      HEX_DIGITS((b & 0xF0) >> 4) + HEX_DIGITS((b & 0x0F))
    }.reduceLeft(_+_)
  }
}