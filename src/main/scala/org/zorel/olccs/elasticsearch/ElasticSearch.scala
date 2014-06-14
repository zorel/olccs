package org.zorel.olccs.elasticsearch

import java.util.Date

import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.node.{Node, NodeBuilder}
import org.elasticsearch.client.Client
import org.elasticsearch.action.index.IndexRequest.OpType
import org.elasticsearch.index.engine.DocumentAlreadyExistsException
import org.elasticsearch.common.xcontent.{XContentBuilder, ToXContent, XContentFactory}
import org.elasticsearch.search.aggregations.AggregationBuilders._
import org.slf4j.LoggerFactory
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonAST.JValue
import com.github.nscala_time.time.Imports._
import org.zorel.olccs.models.{Link, Post}
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.{QueryBuilders, QueryBuilder}
import org.elasticsearch.search.sort.SortOrder
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram


object ElasticSearch {

  // ClientPool initialise (NodeBuilder.nodeBuilder().clusterName("twitter").client(true).node)
  val l = LoggerFactory.getLogger(getClass)

  //  def init(hosts: String) {
  val settings = ImmutableSettings.settingsBuilder()
  settings.put("client.transport.sniff", true)
  settings.put("discovery.zen.ping.multicast.enabled", false)
  settings.put("discovery.zen.ping.unicast.hosts", "127.0.0.1")

  val node: Node = NodeBuilder.nodeBuilder().settings(settings.build).clusterName("olccs").client(true).node
  val client: Client = node.client
  //  }

  implicit val formats = DefaultFormats

  def index(index: String, post: Post) {
    val json = post.to_json
    val id = post.id
    try {
      client.
        prepareIndex(index, "post", id.toString).
        setSource(compact(render(json))).
        setOpType(OpType.CREATE).
        setRefresh(true).
        execute.
        actionGet()
    } catch {
      case ex: DocumentAlreadyExistsException => l.debug("Document dupliqué id " + post.id + " pour tribune " + post.board)
    }
  }

//  def index(index: String, link: Link) {
//    val json = link.to_json
//    val id =
//    try {
//      client.
//        prepareIndex(index, "link").
//        setSource(compact(render(json))).
//        setOpType(OpType.CREATE).
//        setRefresh(false).
//        execute.
//        actionGet()
//    } catch {
//      case ex: DocumentAlreadyExistsException => l.debug("Document dupliqué id " + post.id + " pour tribune " + post.board)
//    }
//  }

  def query(index: String, q: QueryBuilder, size:Int=50): SearchResponse = {
    val r = client.
      prepareSearch(index).
      addFields("id","board","time","info","login","message").
      setTypes("post").
      setQuery(q).
      addSort("id", SortOrder.DESC).
      setSize(size)

//    l.info(r.toString)
    r.execute().
      actionGet()
  }

//  def post_from_horloge(index: String, horloge: String): SearchResponse = {
//    val r = client.
//      prepareSearch(index)
//  }
//  //  def query(index: String, q: JValue): SearchResponse = {
//    query(index, compact(render(q)))
//  }

//  "query_string" -> (
//    ("default_field" -> "message") ~
//      ("default_operator" -> "AND") ~
//      ("query" ->
//        )

  def tribune_stats(index: String): SearchResponse = {
    val r = client.
      prepareSearch(index).
      setTypes("post").
      setQuery(QueryBuilders.matchAllQuery()).
      addAggregation(terms("totoz").field("message.totoz").size(100).
        subAggregation(terms("login").field("login").size(20))).
      addAggregation(terms("domain").field("message.domain").size(100)).
      addAggregation(terms("login").field("login").size(100)).
      setSize(0)
    r.execute().actionGet()
  }

  def tribunes_infos(index: String): SearchResponse = {
    val r = client.
      prepareSearch(index).
      setTypes("post").
      setQuery(QueryBuilders.matchAllQuery()).
      addAggregation(terms("logins").field("login").size(10000)).
      setSize(0)
    r.execute().actionGet()
  }

  def tribunes_trolloscope(index: String): SearchResponse = {
    val date = LocalDateTime.now - 20.minutes

    val r = client.
      prepareSearch(index).
      setTypes("post").
      setQuery(QueryBuilders.rangeQuery("time").gte(date.toString("yyyyMMddHHmmss"))).
      addAggregation(dateHistogram("histogram").field("time").interval(DateHistogram.Interval.minutes(1)).minDocCount(0).extendedBounds(date.toString("yyyyMMddHHmmss"), LocalDateTime.now.toString("yyyyMMddHHmmss")).
        subAggregation(terms("uas").field("info").size(50))).
      setSize(0)
    r.execute().actionGet()
  }

  def login_stats(index: String, login: String): SearchResponse = {
    val r = client.
      prepareSearch(index).
      setTypes("post").
      setQuery(QueryBuilders.termQuery("login", login)).
      addAggregation(terms("totoz").field("message.totoz").size(100)).
      addAggregation(terms("domain").field("message.domain").size(100)).
      addAggregation(missing("posts_without_totoz").field("message.totoz")).
      addAggregation(missing("posts_without_url").field("message.url")).
      addAggregation(dateHistogram("histogram").field("time").interval(DateHistogram.Interval.days(1)).minDocCount(0)).
      setSize(0)
    r.execute().actionGet()
  }

  def optimize() {
    client.admin().indices().
      prepareOptimize("_all").
      setMaxNumSegments(1).
      execute().
      actionGet()
  }

  def close() {
    client.close()
    node.close()
  }
}