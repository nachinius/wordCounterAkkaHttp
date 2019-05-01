package com.nachinius.croesus

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn

object WebServer {
    def main(args: Array[String]) {
        
        implicit val system = ActorSystem("my-system")
        implicit val materializer = ActorMaterializer()
        // needed for the future flatMap/onComplete in the end
        implicit val executionContext = system.dispatcher
        
        val helloRoute =
            path("hello") {
                get {
                    complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
                }
            }
    
        // adding integers as a service
        val route =
            extractRequestContext { ctx =>
                implicit val materializer = ctx.materializer
            
                fileUpload("csv") {
                    case (metadata, byteSource) =>
                    
                        val sumF: Future[Int] =
                        // sum the numbers as they arrive so that we can
                        // accept any size of file
                            byteSource.via(Framing.delimiter(ByteString("\n"), 1024))
                                .mapConcat(_.utf8String.split(",").toVector)
                                .map(_.toInt)
                                .runFold(0) { (acc, n) => acc + n }
                    
                        onSuccess(sumF) { sum => complete(s"Sum: $sum") }
                }
            }
        
        val bindingFuture = Http().bindAndHandle(helloRoute, "localhost", 8080)
        
        println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
        StdIn.readLine() // let it run until user presses return
        bindingFuture
            .flatMap(_.unbind()) // trigger unbinding from the port
            .onComplete(_ => system.terminate()) // and shutdown when done
    }
}

