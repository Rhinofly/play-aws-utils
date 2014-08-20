package fly.play.aws.auth

import fly.play.aws.PlayConfiguration
import play.api.Application

trait AwsCredentials {
  def accessKeyId: String

  def secretKey: String

  def token: Option[String]
}

object AwsCredentials extends ((String, String, Option[String]) => AwsCredentials) {
  def unapply(c: AwsCredentials): Option[(String, String, Option[String])] =
    Option(c) map { c => (c.accessKeyId, c.secretKey, c.token)}

  def apply(accessKeyId: String, secretKey: String, token: Option[String] = None): AwsCredentials =
    SimpleAwsCredentials(accessKeyId, secretKey, token)

  implicit def fromConfiguration(implicit app: Application): AwsCredentials =
    SimpleAwsCredentials(PlayConfiguration("aws.accessKeyId"), PlayConfiguration("aws.secretKey"), PlayConfiguration.optional("aws.token"))
}

case class SimpleAwsCredentials(accessKeyId: String, secretKey: String, token: Option[String] = None) extends AwsCredentials