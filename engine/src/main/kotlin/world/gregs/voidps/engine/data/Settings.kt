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

    private val cached = mutableListOf<CachedSetting<*>>()

    fun load(stream: InputStream): Properties {
        properties.load(stream)
        refreshCached()
        return properties
    }

    fun load(map: Map<String, String>): Properties {
        properties.putAll(map)
        refreshCached()
        return properties
    }

    fun load(properties: Properties): Properties {
        this.properties.putAll(properties)
        refreshCached()
        return this.properties
    }

    fun getOrNull(name: String): String? = properties.getProperty(name)

    operator fun get(name: String): String = properties.getProperty(name)

    operator fun get(name: String, default: String): String = properties.getProperty(name, default)

    operator fun get(name: String, default: Int): Int = getOrNull(name)?.toIntOrNull() ?: default

    operator fun get(name: String, default: Long): Long = getOrNull(name)?.toLongOrNull() ?: default

    operator fun get(name: String, default: Double): Double = getOrNull(name)?.toDoubleOrNull() ?: default

    operator fun get(name: String, default: Boolean): Boolean = getOrNull(name)?.toBooleanStrictOrNull() ?: default

    fun bool(name: String, default: Boolean): CachedSetting<Boolean> = cache(name, default) { it.toBooleanStrictOrNull() }

    fun int(name: String, default: Int): CachedSetting<Int> = cache(name, default) { it.toIntOrNull() }

    fun double(name: String, default: Double): CachedSetting<Double> = cache(name, default) { it.toDoubleOrNull() }

    private fun <T> cache(name: String, default: T, parse: (String) -> T?): CachedSetting<T> {
        val setting = CachedSetting(name, default, parse)
        setting.refresh(this)
        cached.add(setting)
        return setting
    }

    private fun refreshCached() {
        for (setting in cached) {
            setting.refresh(this)
        }
    }

    fun rebase(base: String) {
        for ((key, value) in properties) {
            if (value is String) {
                properties[key] = value.replace("./", base)
            }
        }
        refreshCached()
    }

    fun clear() {
        properties.clear()
        refreshCached()
    }

    companion object : Settings() {
        private const val PROPERTY_FILE_NAME = "game.properties"
        private val logger = InlineLogger()

        fun load(fileName: String = PROPERTY_FILE_NAME): Properties {
            val file = File("./$fileName")
            return if (file.exists()) {
                load(file.inputStream())
            } else {
                logger.debug { "Property file not found; defaulting to internal." }
                load(Settings::class.java.getResourceAsStream("/$fileName")!!)
            }
        }
    }
}
