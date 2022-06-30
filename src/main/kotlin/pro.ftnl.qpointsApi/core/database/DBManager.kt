package pro.ftnl.qpointsApi.core.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import pro.ftnl.qpointsApi.CONFIG
import pro.ftnl.qpointsApi.core.database.structures.AccessTokens
import pro.ftnl.qpointsApi.core.database.structures.QpointsTransactions
import pro.ftnl.qpointsApi.core.database.structures.User
import pro.ftnl.qpointsApi.core.database.structures.Users

/**
 * The database manager.
 */
class DBManager {
    init {
        Database.connect(
            url = "jdbc:mysql://${CONFIG.dbConfig.host}:${CONFIG.dbConfig.port}/${CONFIG.dbConfig.database}",
            driver = "com.mysql.cj.jdbc.Driver",
            user = CONFIG.dbConfig.user,
            password = CONFIG.dbConfig.password
        )

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Users, QpointsTransactions, AccessTokens
            )
            User.getSystemUser()
        }
    }
}