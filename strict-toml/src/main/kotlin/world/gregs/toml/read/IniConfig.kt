package world.gregs.toml.read

import world.gregs.toml.read.ConfigReader.Api


class IniConfig : Api {
    val sections = mutableMapOf<String, MutableMap<String, Any>>()

    override fun set(section: String, key: String, value: Any) {
        getOrCreateSection(section)[key] = value
    }

    private fun getOrCreateSection(section: String): MutableMap<String, Any> {
        return sections.getOrPut(section) { mutableMapOf() }
    }

    // Helper methods to retrieve values
    fun getString(section: String, key: String, default: String = ""): String {
        val value = sections[section]?.get(key) ?: return default
        return value.toString()
    }

    fun getLong(section: String, key: String, default: Long = 0): Long {
        val value = sections[section]?.get(key) ?: return default
        return when (value) {
            is Long -> value
            else -> default
        }
    }

    fun getDouble(section: String, key: String, default: Double = 0.0): Double {
        val value = sections[section]?.get(key) ?: return default
        return when (value) {
            is Double -> value
            else -> default
        }
    }

    fun getBoolean(section: String, key: String, default: Boolean = false): Boolean {
        val value = sections[section]?.get(key) ?: return default
        return when (value) {
            is Boolean -> value
            else -> default
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getList(section: String, key: String): List<Any> {
        val value = sections[section]?.get(key) ?: return emptyList()
        return when (value) {
            is List<*> -> value as List<Any>
            else -> emptyList()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getMap(section: String, key: String): Map<String, Any> {
        val value = sections[section]?.get(key) ?: return emptyMap()
        return when (value) {
            is Map<*, *> -> value as Map<String, Any>
            else -> emptyMap()
        }
    }
}