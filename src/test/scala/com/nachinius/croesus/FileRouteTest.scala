package com.nachinius.croesus

import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, Multipart, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._

class FullTestKitExampleSpec extends WordSpec with Matchers with ScalatestRouteTest {
    
    val smallRoute =
        get {
            pathSingleSlash {
                complete {
                    "Captain on the bridge!"
                }
            } ~
                path("ping") {
                    complete("PONG!")
                }
        }
    
    "The service" should {
        
        "return a greeting for GET requests to the root path" in {
            // tests:
            Get() ~> smallRoute ~> check {
                responseAs[String] shouldEqual "Captain on the bridge!"
            }
        }
        
        "return a 'PONG!' response for GET requests to /ping" in {
            // tests:
            Get("/ping") ~> smallRoute ~> check {
                responseAs[String] shouldEqual "PONG!"
            }
        }
        
        "leave GET requests to other paths unhandled" in {
            // tests:
            Get("/kermit") ~> smallRoute ~> check {
                handled shouldBe false
            }
        }
        
        "return a MethodNotAllowed error for PUT requests to the root path" in {
            // tests:
            Put() ~> Route.seal(smallRoute) ~> check {
                status shouldEqual StatusCodes.MethodNotAllowed
                responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET"
            }
        }
    }
}

class FileRouteTest extends WordSpec with Matchers with ScalatestRouteTest {

    val route = FileRoute.route
    
  def buildMultipartForm(content: String, filename: String = "primes.csv") =  {
          Multipart.FormData(Multipart.FormData.BodyPart.Strict(
              "csv",
              HttpEntity(ContentTypes.`text/plain(UTF-8)`, content),
              Map("filename" -> filename)))
  }
  
  "The route must " in {
    Post("/", buildMultipartForm("a")) ~> route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "Solution(1,Map(a -> 2))"
    }
  }
}
