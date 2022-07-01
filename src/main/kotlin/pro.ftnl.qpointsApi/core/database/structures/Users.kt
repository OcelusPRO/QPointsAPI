package pro.ftnl.qpointsApi.core.database.structures

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import pro.ftnl.qpointsApi.CONFIG
import pro.ftnl.qpointsApi.core.database.structures.Users.discordId
import pro.ftnl.qpointsApi.core.database.structures.Users.id
import pro.ftnl.qpointsApi.core.database.structures.Users.qpoints
import pro.ftnl.qpointsApi.core.database.structures.Users.twitchId

/**
 * Represent a user in the database.
 *
 * @property id         [Int]    The id of the user.
 * @property twitchId   [String] The twitch id of the user.
 * @property discordId  [String] The twitch name of the user.
 * @property qpoints    [Int]    The qpoints of the user.
 */
object Users : Table("${CONFIG.dbConfig.prefix}users") {
    val id = integer("id").autoIncrement()
    val twitchId = varchar("twitch_id", 15).nullable().default(null)
    val discordId = varchar("discord_id", 25).nullable().default(null)
    val qpoints = integer("qpoints").default(0)

    override val primaryKey = PrimaryKey(id, name = "id")
}

/**
 * Represent a user from database.
 *
 * @property id         [Int]    The id of the user.
 * @property twitchId   [String] The twitch id of the user.
 * @property discordId  [String] The twitch name of the user.
 * @property qpoints    [Int]    The qpoints of the user.
 */
data class User(
    val id: Int,
    val twitchId: String? = null,
    var discordId: String? = null,
    var qpoints: Int,
) {

    /**
     * Add qpoints to user from System
     */
    fun addQpoints(qpoints: Int, reason: String? = null) = transfertQpoints(-qpoints, getSystemUser(), reason)

    /**
     * Remove qpoints to user from System
     */
    fun removeQpoints(qpoints: Int, reason: String? = null) = transfertQpoints(qpoints, getSystemUser(), reason)


    /**
     * add qpoints to the user.
     * @param amount [Int] The amount of qpoints to add.
     */
    private fun applyAddQpoints(amount: Int) {
        qpoints += amount
        update()
    }

    /**
     * Remove qpoints from the user.
     * @param amount [Int] The amount of qpoints to remove.
     */
    private fun applyRemoveQpoints(amount: Int) {
        qpoints -= amount
        update()
    }

    /**
     * Transfert qpoints from current user to another.
     *
     * @param amount [Int] The amount of qpoints to transfer.
     * @param user [User] The user to transfer qpoints to.
     */
    fun transfertQpoints(amount: Int, user: User, reason: String?) {
        this.applyRemoveQpoints(amount)
        user.applyAddQpoints(amount)
        QpointsTransaction.create(this, user, amount, reason)
    }

    /**
     * Link discord user to this profile.
     */
    fun linkDiscord(user: User): Boolean {
        if (discordId != null) return false
        if (user.twitchId != null) return false
        if (user == this) return false

        discordId = user.discordId
        transfertQpoints(user.qpoints, user, "Link discord to twitch user")
        user.delete()

        update()
        return true
    }

    /**
     * Update the user in the database.
     */
    private fun update() {
        transaction {
            Users.update({ Users.id eq this@User.id }) {
                it[discordId] = this@User.discordId
                it[qpoints] = this@User.qpoints
            }
        }
    }

    /**
     * Delete the user from the database.
     */
    private fun delete() = transaction { Users.deleteWhere { Users.id eq this@User.id } }


    companion object {

        /**
         * Create a user from a [ResultRow].
         * @param raw [ResultRow] The row to create the user from.
         * @return [User] The user created from the [ResultRow].
         */
        fun fromRaw(raw: ResultRow): User {
            return User(
                raw[id],
                raw[twitchId],
                raw[discordId],
                raw[qpoints]
            )
        }

        private fun createUser(id: String, column: Column<String?>): User {
            transaction {
                Users.insert {
                    it[column] = id
                }
            }
            return getValue(id, column)
        }

        private fun createSystemUser(): User {
            transaction {
                Users.insert {
                    it[id] = 1
                }
            }
            return getById(1) ?: throw IllegalStateException("System user not found")
        }

        /**
         * Get a user from the database by id.
         */
        private fun getValue(id: String, column: Column<String?>): User {
            return transaction {
                var user = Users.select { column eq id }.firstOrNull()?.let { fromRaw(it) }
                if (user == null) user = createUser(id, column)
                return@transaction user
            }
        }

        /**
         * Get a user from the database by discord id.
         */
        fun getByDiscord(id: String): User = getValue(id, discordId)

        /**
         * Get a user from the database by twitch id.
         */
        fun getByTwitch(id: String): User = getValue(id, twitchId)

        /**
         * Get a user from the database by id.
         */
        fun getById(id: Int): User? = transaction { Users.select { Users.id eq id }.map { fromRaw(it) }.firstOrNull() }

        /**
         * get System user.
         */
        fun getSystemUser(): User = getById(1) ?: createSystemUser()


        /**
         * get top 10 of users
         *
         * @return [List] of [User]
         */
        fun getTop10(): List<User> {
            return transaction {
                Users.selectAll().orderBy(qpoints, SortOrder.DESC).limit(10).map { fromRaw(it) }
            }
        }

        fun getAll(): List<User> {
            return transaction {
                Users.selectAll().orderBy(qpoints, SortOrder.DESC).map { fromRaw(it) }
            }
        }

        /**
         * reset all users qpoints
         */
        fun resetQpoints() {
            transaction { Users.update { it[qpoints] = 0 } }
        }


        /**
         * Bulk update users
         *
         * @param users [User] list of users to update
         */
        fun bulkUpdateQpoints(users: List<User>) {
            transaction {
                val currentUsers =
                    Users.select { Users.id inList users.map { it.id } }.filterNotNull().map { fromRaw(it) }
                users.forEach { user ->
                    if (currentUsers.map { it.id }.contains(user.id)) {
                        val toAdd = user.qpoints - currentUsers.first { it.id == user.id }.qpoints
                        Users.update({ Users.id eq user.id }) {
                            it[qpoints] = user.qpoints
                            it[discordId] = user.discordId
                            it[twitchId] = user.twitchId
                        }
                        QpointsTransactions.insert {
                            it[from] = 0
                            it[to] = user.id
                            it[amount] = toAdd
                        }
                    } else {
                        val id = Users.insert {
                            it[discordId] = user.discordId
                            it[twitchId] = user.twitchId
                            it[qpoints] = user.qpoints
                        }[Users.id]
                        QpointsTransactions.insert {
                            it[from] = 0
                            it[to] = id
                            it[amount] = user.qpoints
                        }
                    }
                }
            }
        }
    }
}