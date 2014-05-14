package fly.play.aws.auth

import java.util.Date
import fly.play.aws.PlayConfiguration
import play.api.Application

trait AwsCredentials {
  def accessKeyId: String
  def secretKey: String
}

object AwsCredentials extends ((String, String) => AwsCredentials) {
  def unapply(c: AwsCredentials): Option[(String, String)] =
    Option(c) map { c => (c.accessKeyId, c.secretKey) }

  def apply(accessKeyId: String, secretKey: String): AwsCredentials =
    SimpleAwsCredentials(accessKeyId, secretKey)

  implicit def fromConfiguration(implicit app: Application): AwsCredentials =
    SimpleAwsCredentials(PlayConfiguration("aws.accessKeyId"), PlayConfiguration("aws.secretKey"))
}

case class SimpleAwsCredentials(accessKeyId: String, secretKey: String) extends AwsCredentials