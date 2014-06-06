package fly.play.aws.auth

import play.api.http.{ Writeable, ContentTypeOf }
import java.net.URLEncoder
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Base64
import java.net.URI
import play.api.libs.ws.WSRequestHolder

trait Signer {
  def service:String
  def region:String
  def algorithm:String
  def credentials:AwsCredentials
  def sign(string:String):String
  def sign(request: WSRequestHolder, method: String): WSRequestHolder
  def sign[T](request: WSRequestHolder, method: String, body: T)(implicit wrt: Writeable[T]): WSRequestHolder
  def signUrl(url:String, expiresIn: Int, queryString:Map[String, Seq[String]] = Map.empty):String
}

trait SignerUtils {
  val DEFAULT_ENCODING = "UTF-8"
  protected val EMPTY_HASH = hash(Array.empty[Byte])

  def toHex(b: Array[Byte]): String = b.map("%02x" format _).mkString

  protected def hash(str: String): Array[Byte] = hash(str getBytes DEFAULT_ENCODING)

  protected def hash(bytes: Array[Byte]): Array[Byte] = {
    val md = MessageDigest getInstance "SHA-256"
    md update bytes
    md.digest
  }

  protected def sign(str: String, key: String): Array[Byte] = sign(str, key getBytes DEFAULT_ENCODING)

  protected def sign(str: String, key: Array[Byte]): Array[Byte] = sign(str getBytes DEFAULT_ENCODING, key)

  protected def sign(data: Array[Byte], key: String): Array[Byte] = sign(data, key getBytes DEFAULT_ENCODING)

  protected def sign(data: Array[Byte], key: Array[Byte]): Array[Byte] = {
    val mac = Mac getInstance "HmacSHA256"
    mac init new SecretKeySpec(key, mac.getAlgorithm)
    mac doFinal data
  }

  def base64Encode(data:Array[Byte]):String = new String(Base64 encodeBase64 data)
}