package testUtils

import org.specs2.mutable.Before
import play.api.test.FakeApplication

trait RunningFakePlayApplication extends Before {
  def f = FakeApplication(new java.io.File("./src/test/"))

  def before = play.api.Play.start(f)
}