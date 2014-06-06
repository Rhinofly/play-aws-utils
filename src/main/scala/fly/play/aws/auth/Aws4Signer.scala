package fly.play.aws.auth

import java.util.Date
import fly.play.aws.Aws
import play.api.http.Writeable
import play.api.libs.ws.WSRequestHolder

class Aws4Signer(val credentials: AwsCredentials, val service: String, val region: String) extends Signer with SignerUtils {

  val AwsCredentials(accessKeyId, secretKey) = credentials

  val algorithm = "AWS4-HMAC-SHA256"

  def sign(string: String) = createSignature(string, Scope(currentDate))

  def sign(request: WSRequestHolder, method: String): WSRequestHolder =
    addAuthorizationHeaders(request, method, Some(Array.empty))

  def sign[T](request: WSRequestHolder, method: String, body: T)(implicit wrt: Writeable[T]): WSRequestHolder =
    addAuthorizationHeaders(request, method, Some(wrt transform body))

  def signUrl(url: String, expiresIn: Int, queryString: Map[String, Seq[String]] = Map.empty): String = {
    require(expiresIn >= 1, "expiresIn must at least be 1 second")
    require(expiresIn <= 604800, "expiresIn can be no longer than 7 days (604800 seconds)")

    val scope = Scope(currentDate)

    val queryStringWithRequiredParams = queryString ++
      Map(
        amzAlgorithm,
        amzCredential(scope),
        amzExpires(expiresIn),
        amzSignedHeaders("host"),
        amzDate(scope))

    val request = Request("GET", url, Map.empty, queryStringWithRequiredParams, None)

    val signature = createRequestSignature(scope, request)

    val signedQueryString =
      queryStringWithRequiredParams + amzSignature(signature)

    url + "?" + queryStringAsString(signedQueryString)
  }

  case class Scope(date: Date) {
    private val dateStamp = Aws.dates.dateStampFormat format date

    lazy val value = dateStamp + "/" + region + "/" + service + "/" + TERMINATOR

    lazy val key = {
      var key = sign(dateStamp, "AWS4" + secretKey)
      key = sign(region, key)
      key = sign(service, key)
      key = sign(TERMINATOR, key)
      key
    }

    lazy val dateTime = Aws.dates.dateTimeFormat format date

    lazy val credentials = accessKeyId + "/" + value
  }

  private def addAuthorizationHeaders(wsRequest: WSRequestHolder, method: String, body: Option[Array[Byte]]): WSRequestHolder = {
    val request = Request(
      method,
      wsRequest.url,
      wsRequest.headers,
      wsRequest.queryString,
      body)

    val extraHeaders = createAuthorizationHeaders(request)

    val headers = wsRequest.headers ++ extraHeaders
    val simplifiedHeaders = headers.toSeq.flatMap {
      case (name, values) => values.map(name -> _)
    }
    wsRequest.withHeaders(simplifiedHeaders: _*)
  }

  private def createAuthorizationHeaders(request: Request): Map[String, Seq[String]] = {

    val scope = Scope(currentDate)
    val dateHeader = amzDate(scope)

    val requestWithDateHeader =
      request.copy(headers = request.headers + dateHeader)

    val signature = createRequestSignature(scope, requestWithDateHeader)

    val authorizationHeaderValue =
      createAuthorizationHeader(scope, requestWithDateHeader.signedHeaders, signature)

    val authorizationHeader =
      "Authorization" -> Seq(authorizationHeaderValue)

    Map(dateHeader, authorizationHeader)
  }

  protected def createRequestSignature(scope: Scope, request: Request) = {

    val cannonicalRequest = createCannonicalRequest(request)

    val stringToSign = createStringToSign(scope, cannonicalRequest)

    val signature = createSignature(stringToSign, scope)

    signature
  }

  protected def createCannonicalRequest(request: Request) = {

    val Request(method, _, headers, queryString, body) = request

    val normalizedHeaders = request.normalizedHeaders

    val payloadHash =
      normalizedHeaders
        .get(CONTENT_SHA_HEADER_NAME.toLowerCase)
        .flatMap(_.headOption)
        .orElse(body.map(hexHash))
        .getOrElse("UNSIGNED-PAYLOAD")

    val resourcePath =
      request.uri.getRawPath match {
        case "" | null => "/"
        case path => path
      }

    val cannonicalRequest =
      method + "\n" +
        /* resourcePath */
        resourcePath + "\n" +
        /* queryString */
        queryString
        .map { case (k, v) => k -> v.headOption.getOrElse("") }
        .toSeq.sorted
        .map { case (k, v) => UrlEncoder.encode(k) + "=" + UrlEncoder.encode(v) }
        .mkString("&") + "\n" +
        /* headers */
        normalizedHeaders.map { case (k, v) => k + ":" + v.mkString(" ") + "\n" }.mkString + "\n" +
        /* signed headers */
        request.signedHeaders + "\n" +
        /* payload */
        payloadHash

    cannonicalRequest
  }

  protected def createStringToSign(scope: Scope, cannonicalRequest: String): String =
    algorithm + "\n" +
      scope.dateTime + "\n" +
      scope.value + "\n" +
      toHex(hash(cannonicalRequest))

  protected def createSignature(stringToSign: String, scope: Scope) =
    toHex(sign(stringToSign, scope.key))

  protected def createAuthorizationHeader(
    scope: Scope, signedHeaders: String, signature: String): String =
    algorithm + " " +
      "Credential=" + scope.credentials + "," +
      "SignedHeaders=" + signedHeaders + "," +
      "Signature=" + signature

  private def queryStringAsString(queryString: Map[String, Seq[String]]) =
    queryString.map {
      case (k, v) => UrlEncoder.encode(k) + "=" + v.map(UrlEncoder.encode).mkString(",")
    }.mkString("&")

  private def hexHash(payload: Array[Byte]) = toHex(hash(payload))

  val amzAlgorithm =
    "X-Amz-Algorithm" -> Seq(algorithm)
  def amzDate(scope: Scope) =
    "X-Amz-Date" -> Seq(scope.dateTime)
  def amzCredential(scope: Scope) =
    "X-Amz-Credential" -> Seq(scope.credentials)
  def amzSignature(signature: String) =
    "X-Amz-Signature" -> Seq(signature)
  def amzExpires(expiresIn: Int) =
    "X-Amz-Expires" -> Seq(expiresIn.toString)
  def amzSignedHeaders(headers: String) =
    "X-Amz-SignedHeaders" -> Seq(headers)
  def amzContentSha256(content: Array[Byte]) =
    CONTENT_SHA_HEADER_NAME -> hexHash(content)

  protected def currentDate = new Date
  private val TERMINATOR = "aws4_request"
  private val CONTENT_SHA_HEADER_NAME = "X-Amz-Content-Sha256"

}