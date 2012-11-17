package org.scalatra

import javax.servlet.http.{HttpSession, HttpServletResponse, HttpServletRequest}
import java.io.{ObjectInputStream, ByteArrayInputStream, ByteArrayOutputStream, ObjectOutputStream}
import org.apache.commons.codec.binary.{Base64InputStream, Base64OutputStream}
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.digest.DigestUtils
import servlet.ServletApiImplicits
import javax.crypto.{CipherInputStream, CipherOutputStream, Cipher}
import grizzled.slf4j.Logger

object CryptUtil {
  def createKey(secret: String): SecretKeySpec = {
    val key = DigestUtils.sha256(secret)
    new SecretKeySpec(key, "AES")
  }

  def cipher(keySpec: SecretKeySpec, mode: Int) = {
    val c = Cipher.getInstance("AES")
    c.init(mode, keySpec)

    c
  }
}

trait Session extends Serializable {
  def getAttribute(name: String): Option[Any]
  def setAttribute(name: String, value: Any)

  def apply(name: String) = getAttribute(name) match {
    case Some(value) => value
    case None => throw new NoSuchElementException("No such attribute '%s'".format(name))
  }
}

class HttpSessionWrapper(session: HttpSession) extends Session {
  def getAttribute(name: String) = Option(session.getAttribute(name))
  def setAttribute(name: String, value: Any) { session.setAttribute(name, value) }
}

class MapBackedSession extends Session with Serializable {
  private val sessionMap = new java.util.concurrent.ConcurrentHashMap[String, Any]
  def getAttribute(name: String) = Option(sessionMap.get(name))
  def setAttribute(name: String, value: Any) { sessionMap.put(name, value) }
}

trait SessionStore {
  def read(req: HttpServletRequest): Session
  def write(req: HttpServletRequest, res: HttpServletResponse, session: Session)
}

class HttpSessionStore extends SessionStore {
  def read(req: HttpServletRequest) = new HttpSessionWrapper(req.getSession)

  def write(req: HttpServletRequest, res: HttpServletResponse, session: Session) {
    // nothing to do -- HttpSession already handles this
  }
}

class CookieSessionStore(secretKey: String,
                         cookieName: String = "SESSION",
                         cookieOptions: CookieOptions = CookieOptions()) extends SessionStore with ServletApiImplicits {

  private val encryptionKey = CryptUtil.createKey(secretKey)

  def write(req: HttpServletRequest, res: HttpServletResponse, session: Session) {
    val encryptionCipher = CryptUtil.cipher(encryptionKey, Cipher.ENCRYPT_MODE)
    val byteArrayOutputStream = new ByteArrayOutputStream()
    val base64OutputStream = new Base64OutputStream(byteArrayOutputStream, true, 0, Array[Byte]())
    val cryptOutputStream = new CipherOutputStream(base64OutputStream, encryptionCipher)
    val objectOutputStream = new ObjectOutputStream(cryptOutputStream)

    objectOutputStream.writeObject(session)
    objectOutputStream.close()

    val sessionData = byteArrayOutputStream.toString("ASCII")
    val cookie = Cookie(cookieName, sessionData)(cookieOptions)
    res.addCookie(cookie)
  }

  def read(req: HttpServletRequest) = {
    req.getCookies.find(_.getName == cookieName) match {
      case Some(cookie) => {
        val decryptionCipher = CryptUtil.cipher(encryptionKey, Cipher.DECRYPT_MODE)
        val sessionData = cookie.getValue
        val byteArrayInputStream = new ByteArrayInputStream(sessionData.getBytes("ASCII"))
        val base64InputStream = new Base64InputStream(byteArrayInputStream)
        val decryptInputStream = new CipherInputStream(base64InputStream, decryptionCipher)

        try {
          val objectInputStream = new ObjectInputStream(decryptInputStream)
          objectInputStream.readObject().asInstanceOf[MapBackedSession]
        } catch {
          case e => new MapBackedSession
        }
      }

      case None => new MapBackedSession
    }
  }
}

object ScalatraSession {
  val SessionKey = "org.scalatra.session"
}

trait ScalatraSession extends ScalatraBase {
  import ScalatraSession._

  val log = Logger[ScalatraSession]

  override def handle(request: HttpServletRequest, response: HttpServletResponse) {
    withRequestResponse(request, response) {
      try {
        request += (SessionKey -> sessionStore.read(request))
        super.handle(request, response)
      } finally {
        sessionStore.write(request, response, currentSession)
      }
    }
  }

  /**
   * Specifies the session store to use.
   */
  protected def sessionStore: SessionStore = new HttpSessionStore()

  def currentSession = {
    request.get(SessionKey) match {
      case Some(session) => session.asInstanceOf[Session]
      case None => throw new IllegalStateException("Session is not available outside request")
    }
  }

}

