package org.scalatra
package akka

import _root_.akka.dispatch.Future
import sff4s.Futures
import sff4s.impl.AkkaFuture
import sff4s.impl.AkkaFuture.toFuture

trait AkkaSupport extends AsyncSupport {
  protected def futures: Futures = AkkaFuture

  override protected def renderResponseBody(actionResult: Any) = 
    actionResult match {
      case f: Future[_] => renderFuture(f)
      case x => super.renderResponseBody(x)
    }
}
