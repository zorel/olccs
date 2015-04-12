package org.zorel.olccs.models

/**
 * Created by zorel on 4/12/15.
 */

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap
import scala.collection.JavaConverters._

/**
 * A thread-safe implementation of spray.caching.cache.
 * The cache has a defined maximum number of entries it can store. After the maximum capacity is reached new
 * entries cause old ones to be evicted in a last-recently-used manner, i.e. the entries that haven't been accessed for
 * the longest time are evicted first.
 */
class LruCache[Post](val maxCapacity: Int, val initialCapacity: Int) {
  require(maxCapacity >= 0, "maxCapacity must not be negative")
  require(initialCapacity <= maxCapacity, "initialCapacity must be <= maxCapacity")

  private val store = new ConcurrentLinkedHashMap.Builder[Long, Post]
    .initialCapacity(initialCapacity)
    .maximumWeightedCapacity(maxCapacity)
    .build()

  def get(key: Long) = Option(store.get(key))

  def apply(key: Long, value: Post) = {
    store.putIfAbsent(key, value)
  }

  def remove(key: Long) = Option(store.remove(key))

  def clear(): Unit = {
    store.clear()
  }

  def keys: Set[Long] = store.keySet().asScala.toSet

  def ascendingKeys(limit: Option[Int] = None) =
    limit.map { lim => store.ascendingKeySetWithLimit(lim) }
      .getOrElse(store.ascendingKeySet())
      .iterator()

  def descendingKeys(limit: Option[Int]= None) =
    limit.map { lim => store.descendingKeySetWithLimit(lim)}
      .getOrElse(store.descendingKeySet())
      .iterator()

  def descendingPosts(limit: Int=150) = {
    store.descendingMapWithLimit(limit).asScala.map { case (k,v) => v}.toList
  }

  def size = store.size
}

object LruCache {
  def apply(maxCapacity: Int = 5000, initialCapacity: Int = 150) = {
    new LruCache[Post](maxCapacity, initialCapacity)
  }
}
