package org.zorel.olccs

import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.slf4j.LoggerFactory
import org.zorel.olccs.models.ConfiguredBoard
import org.zorel.olccs.elasticsearch.ElasticSearch
import org.elasticsearch.search.aggregations.Aggregation
import scala.collection.JavaConversions._
import org.scalatra.Ok

/**
 * Created by zorel on 6/9/14.
 */
class StatsServlet extends OlccsStack {
  var l = LoggerFactory.getLogger(getClass)

  get ("/t/:tribune/stats(.:ext)") {
    params.getOrElse("ext","html") match {
      case "json" => {
        contentType = formats("json")
        val r = ElasticSearch.tribune_stats(params("tribune"))
        Ok(r.toString)
      }
      case "html" => {
        contentType = formats("html")
        ssp("/stats/tribune", "tribune" -> params("tribune"))
      }
      case _ => notFound()
    }
  }

  get("/t/:tribune(.html)") {
    val r = ElasticSearch.tribunes_infos(params("tribune"))

    val b: Terms = r.getAggregations.get("logins")

    val logins = for(h: Terms.Bucket <- b.getBuckets) yield {
      l.error("plop")
      h.getKey
    }

    ssp("/stats/index.ssp", "tribune" -> params("tribune"), "logins" -> logins.toList.sorted)
  }

  get("/t/:tribune/trolloscope(.:ext)") {
    params.getOrElse("ext","html") match {
      case "json" => {
        contentType = formats("json")
        val r = ElasticSearch.tribunes_trolloscope(params("tribune"))
        Ok(r.toString)
      }
      case "html" => {
        contentType = formats("html")
        ssp("/stats/trolloscope.ssp", "tribune" -> params("tribune"))
      }
    }
  }

  get("/t/:tribune/calendar(.:ext)") {
    params.getOrElse("ext","html") match {
      case "json" => {
        contentType = formats("json")
        val r = ElasticSearch.tribunes_calendar(params("tribune"))
        Ok(r.toString)
      }
      case "html" => {
        contentType = formats("html")
        ssp("/stats/tribune_calendar.ssp", "tribune" -> params("tribune"))
      }
    }
  }


  get("/t/:tribune/l/:login(.:ext)") {
    params.getOrElse("ext","html") match {
      case "json" => {
        contentType = formats("json")
        val r = ElasticSearch.login_stats(params("tribune"), params("login"))
        Ok(r.toString)
      }
      case "html" => {
        contentType = formats("html")
        ssp("/stats/login", "tribune" -> params("tribune"), "login" -> params("login"))
      }
      case _ => notFound()
    }
  }

}
