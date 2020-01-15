package org.redrune.util

import org.yaml.snakeyaml.Yaml
import java.io.File

/**
 * This class parses data from a .yaml file
 */
object YAMLParser {
    private var map: Map<String, Any>? = null
    private var yaml: Yaml? = null

    fun load(path: String = "./data/settings.yaml") {
        if(yaml == null) {
            yaml = Yaml()
        }

        val file = File(path)
        if(file.exists()) {
            val stream = file.inputStream()
            map = yaml?.load(stream) as Map<String, Any>?
        }
    }

    fun init(string: String) {
        if(yaml == null) {
            yaml = Yaml()
        }

        map = yaml?.load(string) as Map<String, Any>?
    }

    fun clear() {
        yaml = null
        map = null
    }

    fun <T: Any?> get(key: String, default: T): T {
        return if(map?.containsKey(key) == true) {
            map!![key] as T
        } else {
            default
        }
    }

    fun getString(key: String): String? {
        return if(map?.containsKey(key) == true) {
            map!![key]?.toString()
        } else {
            null
        }
    }

    fun getString(key: String, default: String): String {
        return getString(key) ?: default
    }

    fun getInt(key: String): Int? {
        return if(map?.containsKey(key) == true) {
            map!![key]?.toString()?.toIntOrNull()
        } else {
            null
        }
    }

    fun getInt(key: String, default: Int): Int {
        return getInt(key) ?: default
    }

    fun getBool(key: String): Boolean? {
        return if(map?.containsKey(key) == true) {
            val value = map!![key]?.toString()
            when {
                value.equals("true", true) -> true
                value.equals("false", true) -> false
                else -> null
            }
        } else {
            null
        }
    }

    fun getBool(key: String, default: Boolean): Boolean {
        return getBool(key) ?: default
    }

    /**
     * Gets the amount of settings registered
     * @return Int
     */
    fun getSize() : Int = map!!.size
}