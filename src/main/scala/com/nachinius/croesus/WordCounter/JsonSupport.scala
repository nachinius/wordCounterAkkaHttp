package com.nachinius.croesus.WordCounter
import spray.json.{DefaultJsonProtocol, JsNumber, JsObject, JsValue, RootJsonFormat}
import spray.json._

object JsonSupport extends DefaultJsonProtocol {
    import spray.json._
  
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
                    Solution(total.toInt, wordCount.convertTo[Map[String, Int]])
            }
        }
    }
}
