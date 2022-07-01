package pro.ftnl.qpointsApi

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import pro.ftnl.qpointsApi.Configuration.DatabaseConfig
import java.io.File

/**
 * Global instance variable for [Configuration]
 */
val CONFIG: Configuration by lazy { Configuration.loadConfiguration(File("./config.json")) }

/**
 * @author Ocelus
 * @version 1.0.0
 *
 * Configuration class.
 *
 * @property webPort    [Int]               The port to run the web server on.
 * @property dbConfig   [DatabaseConfig]    The database configuration.
 */
data class Configuration(
    val webPort: Int = 8080,
    val dbConfig: DatabaseConfig = DatabaseConfig(),
) {
    /**
     * @author Ocelus
     * @version 1.0.0
     *
     * Exception if error occurs while loading configuration file.
     *
     * @param message The error message.
     */
    class ConfigurationException(message: String) : Exception(message)


    /**
     * Database configuration.
     *
     * @property host       [String]    Database host.
     * @property port       [Int]       Database port.
     * @property database   [String]    Database name.
     * @property user       [String]    Database username.
     * @property password   [String]    Database password.
     * @property prefix     [String]    Database prefix.
     */
    data class DatabaseConfig(
        val prefix: String = "",
        val host: String = "",
        val port: Int = 3306,
        val user: String = "",
        val password: String = "",
        val database: String = "",
    )

    companion object {
        /**
         * Loads the configuration from the given file.
         * @param file [File] The file to load the configuration from.
         * @return The loaded [Configuration].
         * @throws ConfigurationException If the file is not a valid configuration file.
         */
        fun loadConfiguration(file: File): Configuration {
            if (file.createNewFile()) {
                val config = Configuration()
                file.writeText(GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(config))
                throw ConfigurationException("Veuillez remplir le fichier de configuration")
            }
            return try {
                val cfg = Gson().fromJson(file.readText(), Configuration::class.java)
                file.writeText(Gson().toJson(cfg))
                cfg
            } catch (e: Exception) {
                throw ConfigurationException("La configuration n'est pas valide")
            }
        }
    }
}

