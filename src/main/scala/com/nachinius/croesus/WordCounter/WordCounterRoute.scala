package com.nachinius.croesus.WordCounter
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{
  complete,
  extractRequestContext,
  fileUpload,
  onSuccess
}
import akka.stream.scaladsl.Framing
import akka.util.ByteString

object WordCounterRoute {
  
  val mainSeparator = " "
  val extraSeparatorRegex= "[,\t\n.]"
  val maxWordSize = 1024
  val fieldName = "txt"

  val splitWords = Framing.delimiter(ByteString(" "), 256)
  val emptyMap: Map[String, Int] = Map.empty
  // adding integers as a service
  val route =
    extractRequestContext { ctx =>
      implicit val materializer = ctx.materializer

      fileUpload(fieldName) {
        case (metadata, byteSource) =>
          val acc =
            // sum the numbers as they arrive so that we can
            // accept any size of file
            byteSource
              .via(Framing.delimiter(ByteString(mainSeparator), maxWordSize, true))
              .mapConcat(_.utf8String.split(extraSeparatorRegex).toVector)
              .runFold(emptyMap)(Solution.accumulator)
              

          onSuccess(acc) { dict =>
            val sol = Solution(dict)
            val jsValue = JsonSupport.SolutionJsonFormat.write(sol)
            complete(
              HttpEntity(ContentTypes.`application/json`, jsValue.prettyPrint)
            )
          }
      }
    }

}
