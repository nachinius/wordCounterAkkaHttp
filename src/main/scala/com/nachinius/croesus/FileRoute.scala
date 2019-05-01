package com.nachinius.croesus
import akka.http.scaladsl.server.Directives.{complete, extractRequestContext, fileUpload, onSuccess}
import akka.stream.scaladsl.Framing
import akka.util.ByteString

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
