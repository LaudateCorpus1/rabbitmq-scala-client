package com.avast.clients.rabbitmq

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import io.circe.generic.auto._
import io.circe.parser._
import scalaj.http.Http

class TestHelper(host: String, port: Int) {

  final val RootUri = s"http://$host:$port/api"

  def getMessagesCount(queueName: String): Int = {
    val encoded = URLEncoder.encode(queueName, StandardCharsets.UTF_8.toString)

    val resp = Http(s"$RootUri/queues/%2f/$encoded").auth("guest", "guest").asString.body

    println("MESSAGES COUNT:")
    println(resp)

    decode[QueueProperties](resp) match {
      case Right(p) => p.messages
      case r => throw new IllegalStateException(s"Wrong response $r")
    }
  }

  def getPublishedCount(queueName: String): Int = {
    val encoded = URLEncoder.encode(queueName, StandardCharsets.UTF_8.toString)

    val resp = Http(s"$RootUri/queues/%2f/$encoded").auth("guest", "guest").asString.body

    println("PUBLISHED COUNT:")
    println(resp)

    decode[QueueProperties](resp) match {
      case Right(p) =>
        p.message_stats.map(_.publish).getOrElse {
          Console.err.println(s"Could not extract published_count for $queueName!")
          0
        }
      case r => throw new IllegalStateException(s"Wrong response $r")
    }
  }

  def deleteQueue(queueName: String, ifEmpty: Boolean = false, ifUnused: Boolean = false): Unit = {
    println(s"Deleting queue: $queueName")
    val encoded = URLEncoder.encode(queueName, StandardCharsets.UTF_8.toString)

    val resp = Http(s"$RootUri/queues/%2f/$encoded?if-empty=$ifEmpty&if-unused=$ifUnused").method("DELETE").auth("guest", "guest").asString

    val content = resp.body

    val message = s"Delete queue response ${resp.statusLine}: '$content'"
    println(message)

    if (!resp.isSuccess && resp.code != 404) {
      throw new IllegalStateException(message)
    }
  }

  private case class QueueProperties(messages: Int, message_stats: Option[MessagesStats])
  private case class MessagesStats(publish: Int, ack: Option[Int])

}
