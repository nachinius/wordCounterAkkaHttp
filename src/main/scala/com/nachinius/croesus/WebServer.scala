package com.nachinius.croesus

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Multipart.BodyPart
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Framing, Sink}
import akka.util.ByteString
import scala.concurrent.Future
import scala.io.StdIn
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import scala.collection.concurrent.TrieMap
import scala.collection.immutable.HashMap

object WebServer {
  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val route =
      path("hello") {
        get {
          complete(
            HttpEntity(
              ContentTypes.`text/html(UTF-8)`,
              "<h1>Say hello to akka-http</h1>"
            )
          )
        }
      }

    val bindingFuture = Http().bindAndHandle(FileRoute.route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}

object FileRoute {

  val splitWords = Framing.delimiter(ByteString(" "), 256)
  
  type Dict = Map[String, Int]
  
  val emptyMap: Map[String, Int] = Map.empty
  // adding integers as a service
  val route =
    extractRequestContext { ctx =>
      implicit val materializer = ctx.materializer
      
      fileUpload("csv") {
        case (metadata, byteSource) =>
          
          val acc =
          // sum the numbers as they arrive so that we can
          // accept any size of file
            byteSource.via(Framing.delimiter(ByteString(" "), 1024, true))
                .mapConcat(_.utf8String.split("[,\t\n.]").toVector)
                .runFold(emptyMap) {case (dictCounter: Map[String, Int], str: String) =>
                  dictCounter.updated(str, 1 + dictCounter.getOrElse(str,0))
                }
          
          onSuccess(acc) { dict => complete(Solution(sumUp(dict), dict).toString) }
      }
    }
  
  case class Solution(totalWordCount: Int, dictionaryCount: Map[String, Int])
  
  def sumUp(dict: Map[String,Int]): Int = {
    dict.aggregate(0)((prevCount, nextKV) => prevCount + nextKV._2, _ + _)
  }
  
  
}
