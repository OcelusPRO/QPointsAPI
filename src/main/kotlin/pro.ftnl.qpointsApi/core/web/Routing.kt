package pro.ftnl.qpointsApi.core.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.joda.time.DateTime
import pro.ftnl.qpointsApi.core.database.structures.AccessToken
import pro.ftnl.qpointsApi.core.database.structures.User
import pro.ftnl.qpointsApi.core.permDelete
import pro.ftnl.qpointsApi.core.permGet
import pro.ftnl.qpointsApi.core.permPatch
import pro.ftnl.qpointsApi.core.permRoute

fun Application.configureRouting() {
    routing {

        // Des routes pas trop utile, mais leur innutilité cause leur absolue nécessité !
        get("") {
            call.respondText(
                """<h1>Coming soon !</h1><p>The site is currently under construction, so <a href='/make-coffee'>grab a coffee</a>!</p>""".trimIndent(),
                ContentType.Text.Html
            )
        }
        get("/make-coffee") { call.respond(HttpStatusCode(418, "I'm a teapot")) }
        get("/make-tea") { call.respond(HttpStatusCode.BadRequest, "I'm a website not a teapot !") }

        route("/api") {
            authenticate("auth-basic") {
                route("/qpoints") {
                    route("/discord") {
                        permPatch(
                            "/add",
                            AccessToken.EPermission.DISCORD_MANAGE_QPOINTS
                        ) {
                            val toEdit = call.receive<List<ModifyUserQpoints>>()
                            val users = toEdit.mapNotNull { it.toUserUpdated(ModifyUserQpoints.ActionType.ADD) }
                            User.bulkUpdateQpoints(users)
                            call.respond(users)
                        }
                        permPatch(
                            "/remove",
                            AccessToken.EPermission.DISCORD_MANAGE_QPOINTS
                        ) {
                            val toEdit = call.receive<List<ModifyUserQpoints>>()
                            val users = toEdit.mapNotNull { it.toUserUpdated(ModifyUserQpoints.ActionType.REMOVE) }
                            User.bulkUpdateQpoints(users)
                            call.respond(users)
                        }
                        permPatch(
                            "/set",
                            AccessToken.EPermission.DISCORD_MANAGE_QPOINTS
                        ) {
                            val toEdit = call.receive<List<ModifyUserQpoints>>()
                            val users = toEdit.mapNotNull { it.toUserUpdated(ModifyUserQpoints.ActionType.SET) }
                            User.bulkUpdateQpoints(users)
                            call.respond(users)
                        }

                        permPatch(
                            "/transfert/{User1}/{User2}",
                            AccessToken.EPermission.DISCORD_MANAGE_QPOINTS
                        ) {
                            val user1 = call.parameters["User1"] ?: return@permPatch call.respond(
                                HttpStatusCode.BadRequest,
                                "Discord User1 is required"
                            )
                            val user2 = call.parameters["User2"] ?: return@permPatch call.respond(
                                HttpStatusCode.BadRequest,
                                "Discord User2 is required"
                            )
                            val qpoints =
                                call.request.queryParameters["qpoints"]?.toIntOrNull() ?: return@permPatch call.respond(
                                    HttpStatusCode.BadRequest,
                                    "qpoints parameter is required"
                                )
                            val dbUser1 = User.getByDiscord(user1)
                            val dbUser2 = User.getByDiscord(user2)
                            dbUser1.transferPoints(qpoints, dbUser2, "Transfert qpoints beetween two users")
                            call.respond(listOf(dbUser1, dbUser2))
                        }

                        permGet(
                            "/{User}",
                            AccessToken.EPermission.DISCORD_GET_QPOINTS
                        ) {
                            val user = call.parameters["User"] ?: return@permGet call.respond(
                                HttpStatusCode.BadRequest,
                                "Discord User is required"
                            )
                            val dbUser = User.getByDiscord(user)
                            val date = DateTime.now().withDayOfMonth(1).withTimeAtStartOfDay()
                            call.respond(dbUser.withTransactions(date))
                        }

                    }
                    route("/twitch") {
                        permPatch(
                            "/add",
                            AccessToken.EPermission.TWITCH_MANAGE_QPOINTS
                        ) {
                            val toEdit = call.receive<List<ModifyUserQpoints>>()
                            val users = toEdit.mapNotNull { it.toUserUpdated(ModifyUserQpoints.ActionType.ADD) }
                            User.bulkUpdateQpoints(users)
                            call.respond(users)
                        }
                        permPatch(
                            "/remove",
                            AccessToken.EPermission.TWITCH_MANAGE_QPOINTS
                        ) {
                            val toEdit = call.receive<List<ModifyUserQpoints>>()
                            val users = toEdit.mapNotNull { it.toUserUpdated(ModifyUserQpoints.ActionType.REMOVE) }
                            User.bulkUpdateQpoints(users)
                            call.respond(users)
                        }
                        permPatch(
                            "/set",
                            AccessToken.EPermission.TWITCH_MANAGE_QPOINTS
                        ) {
                            val toEdit = call.receive<List<ModifyUserQpoints>>()
                            val users = toEdit.mapNotNull { it.toUserUpdated(ModifyUserQpoints.ActionType.SET) }
                            User.bulkUpdateQpoints(users)
                            call.respond(users)
                        }

                        permPatch(
                            "/transfert/{User1}/{User2}",
                            AccessToken.EPermission.TWITCH_MANAGE_QPOINTS
                        ) {
                            val user1 = call.parameters["User1"] ?: return@permPatch call.respond(
                                HttpStatusCode.BadRequest,
                                "Twitch User1 is required"
                            )
                            val user2 = call.parameters["User2"] ?: return@permPatch call.respond(
                                HttpStatusCode.BadRequest,
                                "Twitch User2 is required"
                            )
                            val qpoints =
                                call.request.queryParameters["qpoints"]?.toIntOrNull() ?: return@permPatch call.respond(
                                    HttpStatusCode.BadRequest,
                                    "qpoints parameter is required"
                                )
                            val dbUser1 = User.getByTwitch(user1)
                            val dbUser2 = User.getByTwitch(user2)
                            dbUser1.transferPoints(qpoints, dbUser2, "Transfert qpoints beetween two users")
                            call.respond(listOf(dbUser1, dbUser2))
                        }

                        permGet(
                            "/{User}",
                            AccessToken.EPermission.TWITCH_GET_QPOINTS
                        ) {
                            val user = call.parameters["User"] ?: return@permGet call.respond(
                                HttpStatusCode.BadRequest,
                                "Twitch User is required"
                            )
                            val dbUser = User.getByTwitch(user)
                            val date = DateTime.now().withDayOfMonth(1).withTimeAtStartOfDay()
                            call.respond(dbUser.withTransactions(date))
                        }

                    }

                    permPatch(
                        "/link/{TwitchUser}/{DiscordUser}",
                        AccessToken.EPermission.LINK_TWITCH_DISCORD
                    ) {
                        val twitchUser = call.parameters["TwitchUser"] ?: return@permPatch call.respond(
                            HttpStatusCode.BadRequest,
                            "Twitch User is required"
                        )
                        val discordUser = call.parameters["DiscordUser"] ?: return@permPatch call.respond(
                            HttpStatusCode.BadRequest,
                            "Discord User is required"
                        )
                        val dbTwitchUser = User.getByTwitch(twitchUser)
                        val dbDiscordUser = User.getByDiscord(discordUser)
                        dbTwitchUser.linkDiscord(dbDiscordUser)
                        call.respond(dbTwitchUser)
                    }

                    permGet(
                        "/top10",
                        AccessToken.EPermission.GET_MULTIPLE_QPOINTS
                    ) {
                        val top10 = User.getTop10()
                        call.respond(top10)
                    }
                    permGet(
                        "/all-users",
                        AccessToken.EPermission.GET_MULTIPLE_QPOINTS
                    ) {
                        val allUsers = User.getAll()
                        call.respond(allUsers)
                    }

                    permDelete(
                        "/",
                        AccessToken.EPermission.RESET_QPOINTS
                    ) {
                        User.resetQpoints()
                        call.respond(HttpStatusCode.OK)
                    }

                }
                route("/admin") {
                    permRoute(
                        "/keys",
                        AccessToken.EPermission.ADMINISTRATOR
                    ) {
                        post {
                            val newKey = call.receive<NewKey>()
                            AccessToken.create(
                                newKey.user,
                                newKey.key,
                                AccessToken.EPermission.fromInt(newKey.permissions)
                            )
                            call.respond(HttpStatusCode.OK)
                        }
                        delete {

                        }
                    }

                }


            }
        }

    }
}


data class NewKey(val user: User, val key: String, val permissions: Int)

data class ModifyUserQpoints(val discordId: String? = null, val twitchId: String? = null, val toTransfer: Int) {
    enum class ActionType { ADD, REMOVE, SET; }

    fun toUserUpdated(action: ActionType): User? {
        if (discordId == null && twitchId == null) return null
        val user = if (twitchId != null) User.getByTwitch(twitchId) else User.getByDiscord(discordId!!)
        user.qpoints = when (action) {
            ActionType.ADD -> user.qpoints + toTransfer
            ActionType.REMOVE -> user.qpoints - toTransfer
            ActionType.SET -> toTransfer
        }
        return user
    }
}