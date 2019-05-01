package com.nachinius.croesus
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.model.headers.{RawHeader, `Content-Type`}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Framing
import akka.util.ByteString

object FileRoute {

  type Dict = Map[String, Int]
  val splitWords = Framing.delimiter(ByteString(" "), 256)
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
            byteSource
              .via(Framing.delimiter(ByteString(" "), 1024, true))
              .mapConcat(_.utf8String.split("[,\t\n.]").toVector)
              .runFold(emptyMap) {
                case (dictCounter: Map[String, Int], str: String) =>
                  dictCounter.updated(str, 1 + dictCounter.getOrElse(str, 0))
              }

          onSuccess(acc) { dict =>
            val sol = Solution(sumUp(dict), dict)
            val jsValue = JsonSupport.SolutionJsonFormat.write(sol)
            complete(HttpEntity(ContentTypes.`application/json`, jsValue.prettyPrint))
          }
      }
    }

  def sumUp(dict: Map[String, Int]): Int = {
    dict.aggregate(0)((prevCount, nextKV) => prevCount + nextKV._2, _ + _)
  }

  final case class Solution(totalWordCount: Int,
                            dictionaryCount: Map[String, Int])

  import akka.http.scaladsl.server.Directives
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
  import spray.json._

  // collect your json format instances into a support trait:
  object JsonSupport extends DefaultJsonProtocol {
    val wordCountFieldName = "wordCount"
    val totalFieldName = "total"
    
    implicit object MapJsonFormat extends RootJsonFormat[Map[String, Int]] {
      override def write(obj: Map[String, Int]): JsValue =
        JsObject(obj.mapValues(JsNumber(_)))
      override def read(json: JsValue): Map[String, Int] = {
        json.asJsObject.fields.mapValues(_.convertTo[Int])
      }
    }
    
    
    implicit object SolutionJsonFormat extends RootJsonFormat[Solution] {
      override def write(obj: Solution): JsValue = JsObject(
        totalFieldName -> JsNumber(obj.totalWordCount),
        wordCountFieldName -> obj.dictionaryCount.toJson
      )
      override def read(json: JsValue): Solution = {
        json.asJsObject.getFields(totalFieldName, wordCountFieldName) match {
          case Seq(JsNumber(total), wordCount) =>
            Solution(total.toInt, wordCount.convertTo[Map[String,Int]])
        }
      }
    }
  }
}
