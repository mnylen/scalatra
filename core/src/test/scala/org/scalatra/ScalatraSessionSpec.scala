package org.scalatra

import test.specs2.MutableScalatraSpec

case class Name(first: String, last: String)
case class Person(name: Name, age: Int)

class ScalatraSessionSpec extends MutableScalatraSpec {
  addServlet(SessionCookieStoreServlet, "/cookie/*")
  addServlet(SessionCookieStoreDifferentSecretServlet, "/cookie2/*")

  "Using CookieSessionStore" should {
    "store session items in cookie 'SESSION'" in {
      post("/cookie/name", Map("name" -> "John")) {
        header("Set-Cookie") must startWith("SESSION=")
      }
    }

    "remember session items on next request" in {
      session {
        post("/cookie/name", Map("name" -> "John")) {
          get("/cookie/name") {
            body must equalTo("Some(John)")
          }
        }
      }
    }

    "use empty session when cookie is not set" in {
      get("/cookie/name") {
        body must equalTo("None")
      }
    }

    "fail to read session data encrypted with different secret key" in {
      post("/cookie/name", Map("name" -> "Bond")) {
        // fix this: session { } does not keep cookies between requests to different
        //           servlet paths
        get("/cookie2/name", Map(), headers = Map("Cookie" -> header("Set-Cookie"))) {
          body must equalTo("None")
        }
      }
    }

    "allow read/write of any serializable value from session" in {
      session {
        post("/cookie/caseClass") {
          get("/cookie/caseClass") {
            body must equalTo("Some(Person(Name(James,Bond),40))")
          }
        }
      }
    }
  }

  class SessionCookieStoreServletBase extends ScalatraServlet with ScalatraSession {
    get("/name") {
      currentSession.getAttribute("name")
    }
    post("/name") {
      currentSession.setAttribute("name", params("name"))
    }
  }

  object SessionCookieStoreServlet extends SessionCookieStoreServletBase {
    override protected def sessionStore = new CookieSessionStore("iamsecret")

    post("/caseClass") {
      currentSession.setAttribute("caseClass", Person(Name("James", "Bond"), 40))
    }

    get("/caseClass") {
      currentSession.getAttribute("caseClass")
    }
  }

  object SessionCookieStoreDifferentSecretServlet extends SessionCookieStoreServletBase {
    override protected def sessionStore = new CookieSessionStore("mysecret")
  }
}
