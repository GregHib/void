package world.gregs.voidps.engine.data

import com.github.michaelbull.logging.InlineLogger
import java.io.File
import java.io.InputStream
import java.util.*

/**
 * Class to load and reload game settings from .property files
 */
open class Settings {

    protected val properties = Properties()

    fun load(stream: InputStream): Properties {
        properties.load(stream)
        return properties
    }

    fun load(map: Map<String, String>): Properties {
        properties.putAll(map)
        return properties
    }

    fun getOrNull(name: String): String? = properties.getProperty(name)

    operator fun get(name: String): String = properties.getProperty(name)

    operator fun get(name: String, default: String): String = properties.getProperty(name, default)

    operator fun get(name: String, default: Int): Int = (properties[name] as? String)?.toIntOrNull() ?: default

    operator fun get(name: String, default: Double): Double = (properties[name] as? String)?.toDoubleOrNull() ?: default

    operator fun get(name: String, default: Boolean): Boolean = (properties[name] as? String)?.toBooleanStrictOrNull() ?: default

    fun clear() {
        properties.clear()
    }

    companion object : Settings() {
        private const val PROPERTY_FILE_NAME = "game.properties"
        private val logger = InlineLogger()

        fun load(): Properties {
            val file = File("./$PROPERTY_FILE_NAME")
            return if (file.exists()) {
                Settings.load(file.inputStream())
            } else {
                logger.debug { "Property file not found; defaulting to internal." }
                Settings.load(Settings::class.java.getResourceAsStream("/$PROPERTY_FILE_NAME")!!)
            }
        }
    }
}