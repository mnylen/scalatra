package com.example

import org.specs._

import org.scalatra.test.specs.ScalatraSpecification

object ExampleSpec extends ScalatraSpecification {
  addFilter(new App with unfiltered.filter.Plan, "/*")

  "The example app" should {
    "serve unfiltered requests" in {
      get("/") {
        status must_== 200
      }
    }
  }
}
