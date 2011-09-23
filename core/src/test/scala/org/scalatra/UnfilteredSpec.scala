package org.scalatra

import org.specs._

import org.scalatra.test.specs.ScalatraSpecification

class UnfilteredApp extends Scalatra {
  get ("/html") {
    <html>
      <head></head>
      <body>Hello html</body>
    </html>
  }

  get ("/hello") {
     "hello world, hello request:"+request.toString
  }

  get ("/") {
     "hello index page!"
  }

  get("/param/:value") {
    "hello %s" format params('value)
  }
}

object UnfilteredSpec extends ScalatraSpecification {
  addFilter(new UnfilteredApp with unfiltered.filter.Plan, "/*")

  "The example app" should {
    "serve unfiltered requests" in {
      get("/") {
        status must_== 200
      }
    }
  }
}
