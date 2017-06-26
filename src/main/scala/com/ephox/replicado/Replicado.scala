package com.ephox.replicado

import argonaut._, Argonaut._

import org.http4s.{Response, argonaut, _}
import org.http4s.argonaut._
import org.http4s.client.blaze._
import org.http4s.client._

import scalaz._
import Scalaz._
import scalaz.concurrent.Task

object Replicado {
  def main(args: Array[String]): Unit = {

    def runReqWithBodyJson[A](f: (Response => Task[(Response, Json)]) => Task[(Response, Json)]): (Response, Json) =
      f { r =>
        getBodyString(r) flatMap { body =>
          JsonParser.parse(body).fold(
            x => Task.fail(new RuntimeException("Json parse failed: " + x)),
            j => Task.delay((r, j))
          )
        }
      }.unsafePerformSync

    def getBodyString(response: Response): Task[String] =
      response.bodyAsText.runFoldMap(x => x)

    def autocorrectCall(client: Client, language: Option[String], baseUrl: String, apiVersion: String, origin: String, key: Option[String]): (Response, Json) = {
      checkSpelling(client, "autocorrect", Method.GET, language.map(makeAutocorrect), baseUrl, apiVersion, origin, key)
    }

    def makeAutocorrect(lang: String): Json =
      jSingleObject("language", jString(lang))

    def checkSpelling(client: Client, call: String, method: Method, body: Option[Json], baseUrl: String, apiVersion: String, origin: String, key: Option[String]): (Response, Json) = {
      val req__ = Request(method = method, uri = Uri.unsafeFromString(s"$baseUrl/$apiVersion/$call"))
        .putHeaders(Header("origin", origin))
      val req_ = key.fold(req__)(k => req__.putHeaders(Header("tiny-api-key", k)))
      val req = body.fold(req_.pure[Task])(x => req_.withBody(x))
      runReqWithBodyJson(client.fetch(req))
    }

    val key = Some("m6fjhknv8bw32q2cmrqwkcy1dpfu49h91ntfb19gic9clouj")


    val client = PooledHttp1Client()
    val baseUrl = "https://spelling-staging.tinymce.com"
    val apiVersion = "1"
    val origin = "https://ephox.com"

    for (j <- 1 to 100000) {
      val chunk = 100
      for (i <- 1 to chunk) {
        val rep: Int = j * chunk + i

        {
          val (resp, j) = autocorrectCall(client, None, baseUrl, apiVersion, origin, key)
          if (resp.status != Status.Ok) throw new RuntimeException("failed at rep: " + rep)
        }

        {
          val (resp, j) = autocorrectCall(client, Some("en_US"), baseUrl, apiVersion, origin, key)
          if (resp.status != Status.Ok) throw new RuntimeException("failed at rep: " + rep)
        }

        {
          val (resp, j) = autocorrectCall(client, Some("bogus"), baseUrl, apiVersion, origin, key)
          if (resp.status != Status.Ok) throw new RuntimeException("failed at rep: " + rep)
        }
      }
      println("Completed reps: " + (j * chunk))
    }
  }
}
