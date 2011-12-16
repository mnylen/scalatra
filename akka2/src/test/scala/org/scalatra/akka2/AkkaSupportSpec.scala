package org.scalatra
package akka2

import _root_.akka.actor._
import Actor._
import test.specs.ScalatraSpecification

object AkkaSupportSpec {
  val system = ActorSystem()

  val probe = system.actorOf(Props(new Actor {
    protected def receive = {
      case "working" => sender ! "the-working-reply"
      case "dontreply" =>
      case "throw" => halt(500, "The error")
    }
  }))
  
  class AkkaSupportServlet extends ScalatraServlet with AkkaSupport {
    protected def akkaDispatcher = system.dispatcher
    implicit val akkaTimeout = system.settings.ActorTimeout
    
    get("/working") {
      probe ? "working"
    }
    
    get("/timeout") {
      probe ? "dontreply"
    }
    
    get("/throw") {
      probe ? "throw"
    }
  }
}

class AkkaSupportSpec extends ScalatraSpecification {
  import AkkaSupportSpec.{AkkaSupportServlet, system}
  addServlet(new AkkaSupportServlet, "/*")

  "The AkkaSupport" should {
    
    "render the reply of an actor" in {
      get("/working") {
        body must_== "the-working-reply"
      }
    }
    
    "respond with timeout if no timely reply from the actor" in {
      get("/timeout") {
        status must_== 504
        body must_== "Gateway timeout"
      }
    }

    "respond with error message" in {
      get("/throw") {
        body must startWith("The error")
      }
    }
  }

  doAfterSpec {
    system.shutdown()
  }
}
