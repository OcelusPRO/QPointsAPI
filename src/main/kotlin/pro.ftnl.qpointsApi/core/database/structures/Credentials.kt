package pro.ftnl.qpointsApi.core.database.structures

import io.github.reactivecircus.cache4k.Cache
import io.ktor.server.auth.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import pro.ftnl.qpointsApi.CONFIG
import pro.ftnl.qpointsApi.core.database.structures.AccessTokens.hash
import pro.ftnl.qpointsApi.core.database.structures.AccessTokens.id
import pro.ftnl.qpointsApi.core.database.structures.AccessTokens.linkedUser
import pro.ftnl.qpointsApi.core.database.structures.AccessTokens.permissions
import pro.ftnl.qpointsApi.core.hash
import kotlin.time.Duration.Companion.minutes


/**
 * Represent a user in the database.
 *
 * @property id             [Int]       The id of the user.
 * @property hash           [String]    The hash of the token.
 * @property linkedUser     [Int]       The id of the user linked to the token.
 * @property permissions    [Int]       The authorizations of the user.
 */
object AccessTokens : Table("${CONFIG.dbConfig.prefix}access_tokens") {
    val id = integer("id").autoIncrement()
    val hash = text("hash")
    val linkedUser = integer("linked_user").uniqueIndex() references Users.id
    val permissions = integer("permissions").default(0)

    override val primaryKey = PrimaryKey(id, name = "id")
}

/**
 * Represent a user from database.
 *
 * @property id             [Int]       The id of the user.
 * @property hash           [String]    The hash of the token.
 * @property linkedUser     [User]      The id of the user linked to the token.
 * @property permissions    [Int]       The authorizations of the user.
 */
data class AccessToken(
    val id: Int,
    val hash: String,
    val linkedUser: User,
    var permissions: Int,
) : Principal {
    enum class EPermission(val bits: Int) {
        ADMINISTRATOR(1),

        TWITCH_MANAGE_QPOINTS(2),
        TWITCH_GET_QPOINTS(2),

        DISCORD_MANAGE_QPOINTS(4),
        DISCORD_GET_QPOINTS(5),

        LINK_TWITCH_DISCORD(6),

        MANAGE_KEYS(7),
        RESET_QPOINTS(8),
        GET_MULTIPLE_QPOINTS(9);

        companion object {
            fun calculate(permissions: List<EPermission>): Int {
                var i = 0
                permissions.forEach { i = i or (1 shl it.bits) }
                return i
            }

            fun fromInt(permissions: Int): List<EPermission> {
                val list = mutableListOf<EPermission>()
                values().forEach { if ((permissions and (1 shl it.bits)) == (1 shl it.bits)) list.add(it) }
                return list
            }
        }
    }

/*
    fun addPermission(vararg permission: EPermission) {
        permission.forEach { permissions = permissions or (1 shl it.bits) }
        update()
    }

    fun removePermission(vararg permission: EPermission) {
        permission.forEach { permissions = permissions and (1 shl it.bits).inv() }
        update()
    }
*/
    private fun hasPermission(permission: Int) = permissions and (1 shl permission) == (1 shl permission)
    fun hasPermission(permission: EPermission) = hasPermission(permission.bits)

/*
    /**
     * Update access token in database.
     */
    private fun update() {
        transaction {
            AccessTokens.update({ AccessTokens.id eq this@AccessToken.id }) {
                it[permissions] = this@AccessToken.permissions
            }
        }
    }



    /**
     * Delete access token from database.
     */
    fun delete() = transaction { AccessTokens.deleteWhere { AccessTokens.id eq this@AccessToken.id } }
 */

    companion object {
        private val cache = Cache.Builder().expireAfterWrite(30.minutes).build<User, AccessToken>()

        /**
         * Create an access_token from a [ResultRow].
         * @param raw [ResultRow] The row to create the user from.
         * @return [AccessToken] The user created from the [ResultRow].
         */
        private fun fromRaw(raw: ResultRow): AccessToken {
            return AccessToken(
                id = raw[id],
                hash = raw[hash],
                linkedUser = User.fromRaw(raw),
                permissions = raw[permissions]
            )
        }

        /**
         * Create new AccessToken.
         *
         * @param user [User] The id of the user linked to the token.
         * @param key [String] The key of the token.
         */
        fun create(user: User, key: String, perms: List<EPermission>): AccessToken {
            return transaction {
                AccessTokens.insert {
                    it[hash] = key.hash("SHA-256")
                    it[linkedUser] = user.id
                    it[permissions] = EPermission.calculate(perms)
                }
                getByUser(user) ?: throw Exception("Failed to create access token")
            }
        }


        /**
         * Get an access_token from the database.
         *
         * @param user [User] The token to get.
         * @return [AccessToken] The access_token.
         */
        fun getByUser(user: User): AccessToken? {
            fun getOnDatabase() = transaction {
                (AccessTokens innerJoin Users).select {
                    linkedUser eq user.id
                }.map { fromRaw(it) }.firstOrNull()
            }

            fun forceGet(): AccessToken? {
                val accessToken = getOnDatabase()
                if (accessToken != null) cache.put(user, accessToken)
                return accessToken
            }
            return cache.get(user) ?: forceGet()
        }
    }
}