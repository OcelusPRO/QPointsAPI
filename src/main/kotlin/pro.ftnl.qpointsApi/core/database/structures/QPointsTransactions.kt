package pro.ftnl.qpointsApi.core.database.structures

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.jodatime.CurrentDateTime
import org.jetbrains.exposed.sql.jodatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import pro.ftnl.qpointsApi.CONFIG
import pro.ftnl.qpointsApi.core.database.structures.QpointsTransactions.amount
import pro.ftnl.qpointsApi.core.database.structures.QpointsTransactions.from
import pro.ftnl.qpointsApi.core.database.structures.QpointsTransactions.id
import pro.ftnl.qpointsApi.core.database.structures.QpointsTransactions.reason
import pro.ftnl.qpointsApi.core.database.structures.QpointsTransactions.to
import pro.ftnl.qpointsApi.core.database.structures.QpointsTransactions.transactionAt

/**
 * Represent a user in the database.
 *
 * @property id             [Int]       The id of the user.
 * @property from           [Int]       ID of the user who qpoints is from
 * @property to             [Int]       ID of the user who qpoints is to
 * @property amount         [Int]       Amount of qpoints
 * @property transactionAt  [DateTime]  Date of transaction
 */
object QpointsTransactions : Table("${CONFIG.dbConfig.prefix}qpoints_transactions") {
    val id = integer("id").autoIncrement()
    val from = integer("from") references Users.id
    val to = integer("to") references Users.id
    val amount = integer("amount")
    val transactionAt = datetime("transactionAt").defaultExpression(CurrentDateTime)
    val reason = varchar("reason", 255).nullable().default(null)
    override val primaryKey = PrimaryKey(id, name = "id")
}

/**
 * Represent a user from database.
 *
 * @property id             [Int]       The id of the user.
 * @property from           [User]      ID of the user who qpoints is from
 * @property to             [User]      ID of the user who qpoints is to
 * @property amount         [Int]       Amount of qpoints
 * @property createdAt      [DateTime]  Date of creation
 */
data class QpointsTransaction(
    val id: Int,
    val from: User,
    val to: User,
    val amount: Int,
    val createdAt: DateTime,
    val reason: String? = null,
) {
    companion object {

        fun fromRaw(raw: ResultRow): QpointsTransaction {
            return QpointsTransaction(
                id = raw[id],
                from = User.fromRaw(raw),
                to = User.fromRaw(raw),
                amount = raw[amount],
                createdAt = raw[transactionAt],
                reason = raw[reason]
            )
        }
/*
        fun getById(id: Int) = transaction {
            QpointsTransactions.select { QpointsTransactions.id eq id }.map { fromRaw(it) }.firstOrNull()
        }

        fun getByUserFrom(user: User, limit: DateTime = DateTime.parse("03/05/2021")) =
            transaction {
                QpointsTransactions.select {
                    from eq user.id and (transactionAt greaterEq limit)
                }.map { fromRaw(it) }.toList()
            }

        fun getByUserTo(user: User, limit: DateTime = DateTime.parse("03/05/2021")) =
            transaction {
                QpointsTransactions.select {
                    to eq user.id and (transactionAt greaterEq limit)
                }.map { fromRaw(it) }.toList()
            }

        fun getByUserAny(user: User, limit: DateTime = DateTime.parse("03/05/2021")) =
            transaction {
                QpointsTransactions.select {
                    transactionAt greaterEq limit and (from eq user.id or (to eq user.id))
                }.map { fromRaw(it) }.toList()
            }
*/

        /**
         * Create a new transaction.
         * @param from            [User]       The user who is sending the qpoints.
         * @param to              [User]       The user who is receiving the qpoints.
         * @param amount          [Int]        The amount of qpoints to send.
         */
        fun create(from: User, to: User, amount: Int, reason: String? = null) = transaction {
            transaction {
                QpointsTransactions.insert {
                    it[QpointsTransactions.from] = from.id
                    it[QpointsTransactions.to] = to.id
                    it[QpointsTransactions.amount] = amount
                    it[QpointsTransactions.reason] = reason
                }
            }
        }

    }
}