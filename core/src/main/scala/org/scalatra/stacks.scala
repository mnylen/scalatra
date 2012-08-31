package org.scalatra

import servlet.FileUploadSupport

/**
 * Stack for building any web applications. Contains
 * support for cookies, session and typed parameters.
 */
trait BaseStack extends ScalatraBase
  with CookieSupport
  with SessionSupport
  with TypedParamSupport

/**
 * Stack for building traditional web applications. Contains
 * everything from base stack + csrf token requirement and
 * support for flash map, method override and file uploads.
 */
trait WebStack extends BaseStack
  with CsrfTokenSupport
  with FlashMapSupport
  with MethodOverride
  with FileUploadSupport


