package fly.play.aws

import play.api.Application
import play.api.PlayException

object PlayConfiguration {
  /**
   * Utility method to allow the retrieval of a key from the settings. Will throw 
   * a PlayException when the key could not be found.
   */
  def apply(key: String)(implicit app: Application): String =
    optional(key).getOrElse(throw new PlayException("Configuration error", "Could not find " + key + " in settings"))

  def optional(key: String)(implicit app: Application): Option[String] = app.configuration.getString(key)
}