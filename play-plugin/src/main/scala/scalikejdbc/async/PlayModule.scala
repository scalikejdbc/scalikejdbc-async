package scalikejdbc.async

import javax.inject.Inject
import play.api.{ Logger, Configuration, Environment }
import play.api.inject.{ ApplicationLifecycle, Module }
import scalikejdbc.{ GlobalSettings, LoggingSQLAndTimeSettings }

import scala.concurrent.Future

class PlayModule @Inject() (lifecycle: ApplicationLifecycle, configuration: Configuration) extends Module {

  import PlayModule._

  // Play DB configuration

  private[this] lazy val playDbConfig = configuration.getConfig("db").getOrElse(Configuration.empty)

  // ScalikeJDBC global configuration

  private[this] lazy val globalConfig = configuration.getConfig("scalikejdbc.global").getOrElse(Configuration.empty)

  private[this] lazy val playConfig = configuration.getConfig("scalikejdbc.play").getOrElse(Configuration.empty)

  private[this] val loggingSQLAndTime = "loggingSQLAndTime"

  private[this] var closeAllOnStop = true

  def onStart(): Unit = {
    playDbConfig.subKeys map {
      name =>
        def load(name: String): (String, String, String, AsyncConnectionPoolSettings) = {
          implicit val config = playDbConfig
          val default = AsyncConnectionPoolSettings()
          val settings = AsyncConnectionPoolSettings(
            maxPoolSize = opt(name, "maxPoolSize").map(v => v.toInt).getOrElse(default.maxPoolSize),
            maxIdleMillis = opt(name, "maxIdleMillis").map(v => v.toLong).getOrElse(default.maxIdleMillis),
            maxQueueSize = opt(name, "maxQueueSize").map(v => v.toInt).getOrElse(default.maxQueueSize)
          )
          (require(name, "url"), opt(name, "user").getOrElse(""), opt(name, "password").getOrElse(""), settings)
        }

        registeredPoolNames.synchronized {
          name match {
            case "global" =>
              // because "db.global" was used as "scalikejdbc.global" previously
              Logger(classOf[PlayModule]).warn("Configuration with \"db.global\" is ignored. Use \"scalikejdbc.global\" instead.")
            case "default" =>
              if (!registeredPoolNames.contains("default")) {
                val (url, user, password, settings) = load(name)
                AsyncConnectionPool.singleton(url, user, password, settings)
                registeredPoolNames.add("default")
              }
            case _ =>
              if (!registeredPoolNames.contains(name)) {
                val (url, user, password, settings) = load(name)
                AsyncConnectionPool.add(Symbol(name), url, user, password, settings)
                registeredPoolNames.add(name)
              }
          }
        }
    }

    opt(loggingSQLAndTime, "enabled")(globalConfig).map(_.toBoolean).foreach {
      enabled =>
        implicit val config = globalConfig
        val default = LoggingSQLAndTimeSettings()
        GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
          enabled = enabled,
          singleLineMode = opt(loggingSQLAndTime, "singleLineMode").map(_.toBoolean).getOrElse(default.singleLineMode),
          logLevel = opt(loggingSQLAndTime, "logLevel").map(v => Symbol(v)).getOrElse(default.logLevel),
          warningEnabled = opt(loggingSQLAndTime, "warningEnabled").map(_.toBoolean).getOrElse(default.warningEnabled),
          warningThresholdMillis = opt(loggingSQLAndTime, "warningThresholdMillis").map(_.toLong).getOrElse(default.warningThresholdMillis),
          warningLogLevel = opt(loggingSQLAndTime, "warningLogLevel").map(v => Symbol(v)).getOrElse(default.warningLogLevel)
        )
    }

    opt("closeAllOnStop", "enabled")(playConfig).foreach { enabled => closeAllOnStop = enabled.toBoolean }

  }

  def onStop(): Unit = {
    if (closeAllOnStop) {
      AsyncConnectionPool.closeAll()
      registeredPoolNames.synchronized(registeredPoolNames.clear())
    }
  }

  lifecycle.addStopHook(() => Future.successful(onStop()))

  def bindings(environment: Environment, configuration: Configuration) = Nil
}

object PlayModule {
  private val registeredPoolNames = scala.collection.mutable.Set.empty[String]

  def opt(name: String, key: String)(implicit config: Configuration): Option[String] = {
    config.getString(name + "." + key)
  }

  def require(name: String, key: String)(implicit config: Configuration): String = {
    config.getString(name + "." + key) getOrElse {
      throw config.reportError(name, "Missing configuration [db." + name + "." + key + "]")
    }
  }
}
