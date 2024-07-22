package griglog.relt

import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.nio.file.Files
import java.util.*

//Based on https://github.com/magistermaks/fabric-simplelibs/tree/master/simple-config
class SimpleConfig private constructor(private val request: ConfigRequest) {
    private val config = HashMap<String, String>()

    /**
     * If any error occurred during loading or reading from the config
     * a 'broken' flag is set, indicating that the config's state
     * is undefined and should be discarded using `delete()`
     *
     * @return the 'broken' flag of the configuration
     */
    private var isBroken: Boolean = false

    init {
        val identifier = "Config '" + request.filename + "'"

        if (!request.file.exists()) {
            LOGGER.info("$identifier is missing, generating default one...")

            try {
                createConfig()
            } catch (e: IOException) {
                LOGGER.error("$identifier failed to generate!")
                LOGGER.trace(e)
                isBroken = true
            }
        }

        if (!isBroken) {
            try {
                loadConfig()
            } catch (e: Exception) {
                LOGGER.error("$identifier failed to load!")
                LOGGER.trace(e)
                isBroken = true
            }
        }
    }

    fun interface DefaultConfig {
        fun get(namespace: String): String
    }

    class ConfigRequest(val file: File, val filename: String, var provider: DefaultConfig = DefaultConfig { "" }) {

        /**
         * Sets the default config provider, used to generate the
         * config if it's missing.
         *
         * @param provider default config provider
         * @return current config request object
         * @see DefaultConfig
         */
        fun provider(provider: DefaultConfig): ConfigRequest {
            this.provider = provider
            return this
        }

        /**
         * Loads the config from the filesystem.
         *
         * @return config object
         * @see SimpleConfig
         */
        fun request(): SimpleConfig {
            return SimpleConfig(this)
        }

        fun getConfig(): String {
            return provider.get(filename) + "\n"
        }
    }

    private fun createConfig() {
        // try creating missing files

        request.file.parentFile.mkdirs()
        Files.createFile(request.file.toPath())

        // write default config data
        val writer = PrintWriter(request.file, "UTF-8")
        writer.write(request.getConfig())
        writer.close()
    }

    private fun loadConfig() {
        val reader = Scanner(request.file)
        var line = 1
        while (reader.hasNextLine()) {
            parseConfigEntry(reader.nextLine(), line)
            line++
        }
        reader.close()
    }

    private fun parseConfigEntry(entry: String, line: Int) {
        if (entry.isNotEmpty() && !entry.startsWith("#")) {
            val parts = entry.split("=".toRegex(), limit = 2).toTypedArray()
            if (parts.size == 2) {
                config[parts[0]] = parts[1]
            } else {
                throw RuntimeException("Syntax error in config file on line $line!")
            }
        }
    }

    /**
     * Returns string value from config corresponding to the given
     * key, or the default string if the key is missing.
     *
     * @return  value corresponding to the given key, or the default value
     */
    fun getOrDefault(key: String, def: String): String =
        config[key] ?: def

    /**
     * Returns integer value from config corresponding to the given
     * key, or the default integer if the key is missing or invalid.
     *
     * @return  value corresponding to the given key, or the default value
     */
    fun getOrDefault(key: String, def: Int): Int =
        config[key]?.toIntOrNull() ?: def

    /**
     * Returns boolean value from config corresponding to the given
     * key, or the default boolean if the key is missing.
     *
     * @return  value corresponding to the given key, or the default value
     */
    fun getOrDefault(key: String, def: Boolean): Boolean =
        config[key]?.equals("true", ignoreCase = true) ?: def

    /**
     * Returns double value from config corresponding to the given
     * key, or the default string if the key is missing or invalid.
     *
     * @return  value corresponding to the given key, or the default value
     */
    fun getOrDefault(key: String, def: Double): Double =
        config[key]?.toDoubleOrNull() ?: def

    /**
     * deletes the config file from the filesystem
     *
     * @return true if the operation was successful
     */
    fun delete(): Boolean {
        LOGGER.warn("Config '" + request.filename + "' was removed from existence! Restart the game to regenerate it.")
        return request.file.delete()
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger("SimpleConfig")

        /**
         * Creates new config request object, ideally `namespace`
         * should be the name of the mod id of the requesting mod
         *
         * @param filename - name of the config file
         * @return new config request object
         */
        fun of(filename: String): ConfigRequest {
            val path = FabricLoader.getInstance().configDir
            return ConfigRequest(path.resolve("$filename.properties").toFile(), filename)
        }
    }
}