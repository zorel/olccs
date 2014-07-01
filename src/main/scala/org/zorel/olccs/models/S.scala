package org.zorel.olccs.models

import org.zorel.olccs.OlccsConfig

import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession

case class S(id: Option[Long], key: String, content: String, user_id: Long)

object Storage extends Table[S]("storage") {
  val db = OlccsConfig.db
  def id = column[Long]("id", O.AutoInc)
  def key = column[String]("key", O.NotNull)
  def content = column[String]("content")
  def user_id = column[Long]("user_id")

  def user = foreignKey("user_fk", user_id, User)(_.id)

  def * = id.? ~ key ~ content ~ user_id <> (S.apply _, S.unapply _)

}