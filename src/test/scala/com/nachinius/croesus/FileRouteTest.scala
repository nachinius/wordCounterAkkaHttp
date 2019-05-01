package com.nachinius.croesus

import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.model.{
  ContentTypes,
  HttpEntity,
  Multipart,
  StatusCodes
}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._
import com.nachinius.croesus.FileRoute.Solution

class FileRouteTest extends WordSpec with Matchers with ScalatestRouteTest {

  val route = FileRoute.route

  def buildMultipartForm(content: String, filename: String = "primes.csv") = {
    Multipart.FormData(
      Multipart.FormData.BodyPart.Strict(
        "csv",
        HttpEntity(ContentTypes.`text/plain(UTF-8)`, content),
        Map("filename" -> filename)
      )
    )
  }
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import spray.json._
  import FileRoute.JsonSupport._

  "The endpoint " must {
    "handle a single word" in {
      Post("/", buildMultipartForm("a")) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Solution] shouldEqual Solution(1, Map("a" -> 1))
      }
    }
    "handle more than one word" in {
      Post("/", buildMultipartForm("lorem ipsum")) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Solution] shouldBe Solution(
          2,
          Map("lorem" -> 1, "ipsum" -> 1)
        )
      }
    }
    "handle repeated words" in {
      Post("/", buildMultipartForm("lorem ipsum lorem")) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        val sol = responseAs[Solution]
        sol.countOf("lorem") shouldBe 2
        sol.countOf("ipsum") shouldBe 1
      }
    }
    "handle several repeated words" in {
      Post("/", buildMultipartForm("lorem ipsum lorem ipsum ipsum")) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        val sol = responseAs[Solution]
        sol.countOf("lorem") shouldBe 2
        sol.countOf("ipsum") shouldBe 3
      }
    }
    "disregard empty spaces" in {
      Post("/", buildMultipartForm("lorem   ipsum    lorem    ipsum ipsum")) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        val sol = responseAs[Solution]
        sol.countOf("lorem") shouldBe 2
        sol.countOf("ipsum") shouldBe 3
      }
    }
    "disregard new lines" in {
      Post("/", buildMultipartForm("lorem \n  ipsum  \n  lorem   \n\n\n ipsum ipsum")) ~> route ~>
          check {
        status shouldEqual StatusCodes.OK
        val sol = responseAs[Solution]
        sol.countOf("lorem") shouldBe 2
        sol.countOf("ipsum") shouldBe 3
      }
    }
    
  }

}
