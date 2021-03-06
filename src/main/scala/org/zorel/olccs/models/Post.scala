package org.zorel.olccs.models

import scala.xml.transform.{RuleTransformer, RewriteRule}
import scala.xml._
import org.slf4j.LoggerFactory
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._

class Post(val board: String, val id: Long, val time: String, val info: String, val login: String, val message: String) {

  val l = LoggerFactory.getLogger(getClass)

  val p: JValue = ("board" -> board) ~
    ("id" -> id) ~
    ("time" -> time) ~
    ("info" -> info) ~
    ("login" -> login) ~
    ("message" -> message) ~
    ("type" -> "post")


  def to_s: String = {
    compact(render(p))
  }

  def to_json: JValue = {
    p
  }

  def to_xml: Elem = {
    <post board={board} id={id.toString} time={time}>
      <info>{info}</info>
      <login>{login}</login>
      <message>{xml.Unparsed(message)}</message>
    </post>
  }

  def to_tsv: String = {
    "%s\t%d\t%s\t%s\t%s\t%s\n".format(board, id.toLong, time, info, login, message)
  }
}

object Post {
  val l = LoggerFactory.getLogger(getClass)

  def apply(board: String, id: String, time: String, info: String, login: String, message: String) = {
    new Post(board, id.toLong, time, info, login, message)
  }

  def apply(board: String, id: Long, time: String, info: String, login: String, message: String) = {
    new Post(board, id, time, info, login, message)
  }

  // Used to build from remote. Must remove the <message> and </message>
  def apply(board: String, id: String, time: String, info: String, login: String, message: NodeSeq) = {
    val rule = new RewriteRule {
      override def transform(n: Node) = n match {
        case e @ <clock>{_}</clock> => new Atom(e.text)
        case e @ <a>{the_text}</a> => e.attributes.get("class").getOrElse(Text("")).text match {
          case "smiley" => new Atom(the_text)
          case "smiley snap_nopreview" => new Atom(the_text)
          case _ => e
        }
        case _ => n
      }
    }

//    (message \ "a").map { a =>
////      l.info("a trouvé dans message, href= %s".format(a.attributes.get("href").getOrElse(Text("")).text))
////      Link(a.attributes.get("href").getOrElse(Text("")).text, board)
//    }

    val t = new RuleTransformer(rule).transform(message.asInstanceOf[Elem]).toString()
    val m = if(t == "<message/>")
      "<message></message>"
    else
      t.replaceAll("""(?m)\s+$""", "")

//    l.info(m)
//    l.info(id)
    new Post(board, id.toLong, time, info, login, m.substring(9 , m.length-10))
  }

}