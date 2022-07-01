package pro.ftnl.qpointsApi.core

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import pro.ftnl.qpointsApi.core.database.structures.AccessToken
import pro.ftnl.qpointsApi.core.web.PermRoutes


fun Route.permPost(
    path: String,
    vararg permissions: AccessToken.EPermission,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route {
    val route = post(path, body)
    PermRoutes.routesPerms[route] = permissions.toList()
    return route
}

fun Route.permGet(
    path: String,
    vararg permissions: AccessToken.EPermission,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route {
    val route = get(path, body)
    println(route.toString())
    PermRoutes.routesPerms[route] = permissions.toList()
    return route
}

fun Route.permPatch(
    path: String,
    vararg permissions: AccessToken.EPermission,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route {
    val route = patch(path, body)
    println(route.toString())
    PermRoutes.routesPerms[route] = permissions.toList()
    return route
}

fun Route.permDelete(
    path: String,
    vararg permissions: AccessToken.EPermission,
    body: PipelineInterceptor<Unit, ApplicationCall>,
): Route {
    val route = delete(path, body)
    println(route.toString())
    PermRoutes.routesPerms[route] = permissions.toList()
    return route
}

fun Route.permRoute(
    path: String,
    vararg permissions: AccessToken.EPermission,
    build: Route.() -> Unit,
): Route {
    val route = route(path, build)
    println(route.toString())
    PermRoutes.routesPerms[route] = permissions.toList()
    return route
}

fun ApplicationCall.toRoutePerm(): Route? =
    PermRoutes.routesPerms.map { it.key }.find { it.toString().contains("${request.uri}/(method:") }
