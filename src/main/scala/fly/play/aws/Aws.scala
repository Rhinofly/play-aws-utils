package fly.play.aws

import java.text.SimpleDateFormat
import java.util.{Locale, SimpleTimeZone}

import fly.play.aws.auth.Signer
import play.api.http.{ContentTypeOf, Writeable}
import play.api.libs.iteratee.Iteratee
import play.api.Application

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.{WS, WSRequestHolder, WSResponse, WSResponseHeaders}

/**
 * Amazon Web Services
 */
object Aws {

  def withSigner(signer: Signer) = AwsRequestBuilder(signer)

  case class AwsRequestBuilder(signer: Signer) {
    def url(url: String)(implicit app:Application): AwsRequestHolder = 
      AwsRequestHolder(WS.url(url).withFollowRedirects(true), signer)
  }

  case class AwsRequestHolder(wrappedRequest: WSRequestHolder, signer: Signer) {
    def headers = wrappedRequest.headers
    def queryString = wrappedRequest.queryString

    def withHeaders(headers: (String, String)*): AwsRequestHolder =
      this.copy(wrappedRequest = wrappedRequest.withHeaders(headers: _*))

    def withQueryString(parameters: (String, String)*): AwsRequestHolder =
      this.copy(wrappedRequest = wrappedRequest.withQueryString(parameters: _*))

    val sign = signer.sign(wrappedRequest, _: String)
    def sign[T](method: String, body: T)(implicit wrt: Writeable[T], ct: ContentTypeOf[T]) =
      signer.sign(wrappedRequest, method, body)

    def get(): Future[WSResponse] =
      sign("GET").get

    def get[A](consumer: WSResponseHeaders => Iteratee[Array[Byte], A])(implicit ec:ExecutionContext): Future[Iteratee[Array[Byte], A]] =
      sign("GET").get(consumer)

    def post[T](body: T)(implicit wrt: Writeable[T], ct: ContentTypeOf[T]): Future[WSResponse] =
      sign("POST", body).post(body)

    def postAndRetrieveStream[A, T](body: T)(consumer: WSResponseHeaders => Iteratee[Array[Byte], A])(implicit wrt: Writeable[T], ct: ContentTypeOf[T], ec:ExecutionContext): Future[Iteratee[Array[Byte], A]] =
      sign("POST", body).postAndRetrieveStream(body)(consumer)

    def put[T](body: T)(implicit wrt: Writeable[T], ct: ContentTypeOf[T]): Future[WSResponse] =
      sign("PUT", body).put(body)

    def put:Future[WSResponse] =
      put("")

    def putAndRetrieveStream[A, T](body: T)(consumer: WSResponseHeaders => Iteratee[Array[Byte], A])(implicit wrt: Writeable[T], ct: ContentTypeOf[T], ec:ExecutionContext): Future[Iteratee[Array[Byte], A]] =
      sign("PUT", body).putAndRetrieveStream(body)(consumer)

    def delete(): Future[WSResponse] =
      sign("DELETE").delete

    def head(): Future[WSResponse] =
      sign("HEAD").head

    def options(): Future[WSResponse] =
      sign("OPTIONS").options

  }

  object dates {
    lazy val timeZone = new SimpleTimeZone(0, "UTC")

    def dateFormat(format: String, locale:Locale = Locale.getDefault): SimpleDateFormat = {
      val df = new SimpleDateFormat(format, locale)
      df setTimeZone timeZone
      df
    }

    lazy val dateTimeFormat = dateFormat("yyyyMMdd'T'HHmmss'Z'")
    lazy val dateStampFormat = dateFormat("yyyyMMdd")

    lazy val iso8601DateFormat = dateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    lazy val rfc822DateFormat = dateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

  }
}