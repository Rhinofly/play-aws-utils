package fly.play.aws.auth

import org.specs2.mutable.Before
import org.specs2.mutable.Specification
import play.api.Play.current
import play.api.test.FakeApplication
import testUtils.RunningFakePlayApplication

object AwsCredentialsSpec extends Specification with RunningFakePlayApplication {

  "AwsCredentials" should {
    "retrieve from configuration" in {
      AwsCredentials.fromConfiguration must_== AwsCredentials("testKey", "testSecret")
    }
    "implement apply" in {
      AwsCredentials("key", "secret")
      ok
    }
    "implement unapply" >> {
      val AwsCredentials(a, b) = AwsCredentials("key", "secret")
      a must_== "key"
      b must_== "secret"
    }

    def checkImplicit()(implicit c: AwsCredentials) = c

    "provide an implicit value" in {
      checkImplicit must not beNull
    }

    "override the implicit" in {
      checkImplicit()(AwsCredentials("test", "test")) must_== AwsCredentials("test", "test")
    }
  }
}