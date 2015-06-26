package scalikejdbc.async

import play.api.inject.Module
import play.api.{ Configuration, Environment }

class PlayModule extends Module {
  def bindings(environment: Environment, configuration: Configuration) = Seq(
    bind[PlayInitializer].toSelf.eagerly()
  )
}

