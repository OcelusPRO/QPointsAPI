package pro.ftnl.qpointsApi

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory
import pro.ftnl.qpointsApi.core.database.DBManager
import pro.ftnl.qpointsApi.core.web.configureHTTP
import pro.ftnl.qpointsApi.core.web.configureRouting
import pro.ftnl.qpointsApi.core.web.configureSecurity
import pro.ftnl.qpointsApi.core.web.configureSerialization


fun main() {
    DBManager()
    val environment = applicationEngineEnvironment {
        log = LoggerFactory.getLogger("ktor.application")
        connector {
            port = CONFIG.webPort
        }
        module(Application::configureHTTP)
        module(Application::configureRouting)
        module(Application::configureSecurity)
        module(Application::configureSerialization)
    }
    embeddedServer(Netty, environment).start(wait = true)
}