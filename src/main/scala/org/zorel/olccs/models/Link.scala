package org.zorel.olccs.models

import java.security.MessageDigest
import akka.actor.{ActorSystem, Props, Actor}
import org.zorel.olccs.actors.LinkActor
import akka.routing.RoundRobinRouter
import org.slf4j.LoggerFactory
import uk.co.bigbeeconsultants.http.{Config, HttpClient}
import de.l3s.boilerpipe.extractors.ArticleExtractor
import java.net.URL
import java.io.File
import org.xhtmlrenderer.util.FSImageWriter
import org.xhtmlrenderer.swing.Java2DRenderer
import org.xhtmlrenderer.simple.Graphics2DRenderer
import java.awt.{Graphics2D, Dimension}
import java.awt.image.BufferedImage
import org.jsoup.Jsoup


class Link(val url: String, val index: String) {
  val l = LoggerFactory.getLogger(getClass)
  val id = MessageDigest.getInstance("SHA-1").digest(url.getBytes())

  def to_json {

  }

  def process {

    val config = Config(followRedirects = false,
      keepAlive = false,
      userAgentString = Some("olccs-scala")
    )

    val http_client = new HttpClient(config)
    val response = http_client.get(new URL(url))
    val body = response.body.asString
    val f = new File(id + ".png")

    val doc = Jsoup.parse(body)

    val renderer = new Graphics2DRenderer()
    renderer.setDocument(url)

    val dim: Dimension = new Dimension(1024,4096)
    val buff: BufferedImage = new BufferedImage(dim.getWidth.asInstanceOf[Int], dim.getHeight.asInstanceOf[Int], BufferedImage.TYPE_INT_ARGB)

    val g: Graphics2D = buff.getGraphics.asInstanceOf[Graphics2D]

    renderer.layout(g, dim)
    renderer.render(g)

    g.dispose



    // write it out, full size, PNG
    // FSImageWriter instance can be reused for different images,
    // defaults to PNG
    val imageWriter = new FSImageWriter();
    imageWriter.write(buff, id + ".png");


    if (response.body.contentType.isTextual) {
      l.info("Link textual")
      System.out.println(ArticleExtractor.INSTANCE.getText(body))
    }
  }
}

object Link {
  val l = LoggerFactory.getLogger(getClass)

  def apply(url: String, index: String) {
    l.debug("Link received, : %s".format(url))
    val context = ActorSystem("OlccsSystem")
    val worker = context.actorOf(Props[LinkActor].withRouter(RoundRobinRouter(10)), name = "LinkActor")
    val link = new Link(url, index)
    worker ! link

  }
}
