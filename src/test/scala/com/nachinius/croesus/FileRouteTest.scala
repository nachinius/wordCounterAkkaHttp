package com.nachinius.croesus

import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, Multipart, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._
import com.nachinius.croesus.FileRoute.Solution

class FileRouteTest extends WordSpec with Matchers with ScalatestRouteTest {

    val route = FileRoute.route
    
  def buildMultipartForm(content: String, filename: String = "primes.csv") =  {
          Multipart.FormData(Multipart.FormData.BodyPart.Strict(
              "csv",
              HttpEntity(ContentTypes.`text/plain(UTF-8)`, content),
              Map("filename" -> filename)))
  }
    import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
    import spray.json._
  import FileRoute.JsonSupport._
  
  "The route must " in {
    Post("/", buildMultipartForm("a")) ~> route ~> check {
      status shouldEqual StatusCodes.OK
//        responseAs[String] shouldEqual "Solution(1,Map(a -> 3))"
        responseAs[Solution] shouldEqual Solution(1, Map("a" -> 4))
    }
  }
}
