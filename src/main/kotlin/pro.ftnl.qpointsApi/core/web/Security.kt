package pro.ftnl.qpointsApi.core.web

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import pro.ftnl.qpointsApi.core.database.structures.AccessToken
import pro.ftnl.qpointsApi.core.database.structures.User
import pro.ftnl.qpointsApi.core.hash
import pro.ftnl.qpointsApi.core.toRoutePerm

/**
 * Manage security
 */
fun Application.configureSecurity() {

    install(Authentication) {

        basic("auth-basic") {
            realm = "Access to API routes"
            validate { credentials ->
                val userId = credentials.name.toIntOrNull() ?: return@validate null
                val user = User.getById(userId) ?: return@validate null
                val accessToken = AccessToken.getByUser(user) ?: return@validate null
                if (accessToken.hash != credentials.password.hash("SHA-256")) return@validate null
                if (accessToken.hasPermission(AccessToken.EPermission.ADMINISTRATOR)) return@validate accessToken
                if (PermRoutes.routesPerms[toRoutePerm()]?.all { accessToken.hasPermission(it) } == true) return@validate accessToken
                null
            }
        }
    }
}

object PermRoutes {
    val routesPerms = mutableMapOf<Route, List<AccessToken.EPermission>>()
}
