package org.zorel.olccs

import org.scalatra._
import scalate.ScalateSupport
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import javax.servlet.http.HttpServletRequest
import collection.mutable
import org.zorel.olccs.auth.AuthenticationSupport

trait OlccsStack extends ScalatraServlet with ScalateSupport
  with ApiFormats
  with CorsSupport
  with AuthenticationSupport
  with Instrumented {

  notFound(
    "Not found"
  )
  /* wire up the precompiled templates */

  override protected implicit def string2RouteMatcher(path: String): RouteMatcher = RailsPathPatternParser(path)
  addMimeMapping("text/tsv", "tsv")
  override protected def defaultTemplatePath: List[String] = List("/WEB-INF/templates/views")
  override protected def createTemplateEngine(config: ConfigT) = {
    val engine = super.createTemplateEngine(config)
    engine.layoutStrategy = new DefaultLayoutStrategy(engine,
      TemplateEngine.templateTypes.map("/WEB-INF/templates/layouts/default." + _): _*)
    engine.packagePrefix = "templates"
    engine
  }
  /* end wiring up the precompiled templates */
  
  override protected def templateAttributes(implicit request: HttpServletRequest): mutable.Map[String, Any] = {
    super.templateAttributes ++ mutable.Map.empty // Add extra attributes here, they need bindings in the build file
  }

  notFound {
    // remove content type in case it was set through an action
    contentType = null
    // Try to render a ScalateTemplate if no route matched
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }
}
