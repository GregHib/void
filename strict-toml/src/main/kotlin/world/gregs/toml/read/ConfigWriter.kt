package world.gregs.toml.read

import java.io.File
import java.io.Writer

// Extension of the IniConfig class to include encoding functionality
class ConfigWriter {

    // Encoding functionality
    fun encode(file: File, config: IniConfig) {
        file.bufferedWriter().use { writer ->
            encodeTo(writer, config)
        }
    }

    fun encodeTo(writer: Writer, config: IniConfig) {
        val sectionParentMap = buildSectionHierarchy(config)

        // Write sections in an order that ensures parents come before children
        val orderedSections = orderSections(config, sectionParentMap)

        for (section in orderedSections) {
            // Write section header
            writer.write("[$section]\n")

            // Write section contents
            config.sections[section]?.forEach { (key, value) ->
                encodeKeyValue(writer, key, value)
            }

            // Add a blank line after each section
            writer.write("\n")
        }
    }

    private fun orderSections(config: IniConfig, sectionParentMap: Map<String, String?>): List<String> {
        val result = mutableListOf<String>()
        val visited = mutableSetOf<String>()

        fun visit(section: String) {
            if (section in visited) return
            visited.add(section)

            // First visit parent if exists
            sectionParentMap[section]?.let { parent ->
                visit(parent)
            }

            result.add(section)
        }

        // Visit all sections
        config.sections.keys.forEach { visit(it) }

        return result
    }

    private fun buildSectionHierarchy(config: IniConfig): Map<String, String?> {
        val result = mutableMapOf<String, String?>()

        for (section in config.sections.keys) {
            val lastDotIndex = section.lastIndexOf('.')
            if (lastDotIndex > 0) {
                val parent = section.substring(0, lastDotIndex)
                result[section] = parent
            } else {
                result[section] = null
            }
        }

        return result
    }

    private fun encodeKeyValue(writer: Writer, key: String, value: Any) {
        val needsQuotes = key.contains(' ') || key.contains('\t') || key.contains('=')

        // Write key
        if (needsQuotes) {
            writer.write("\"${escapeQuotes(key)}\"")
        } else {
            writer.write(key)
        }

        writer.write(" = ")

        // Write value based on type
        when (value) {
            is String -> encodeString(writer, value)
            is Long, is Int -> writer.write(value.toString())
            is Double, is Float -> writer.write(value.toString())
            is Boolean -> writer.write(value.toString())
            is List<*> -> encodeList(writer, value)
            is Map<*, *> -> encodeMap(writer, value)
            else -> writer.write(value.toString())
        }

        writer.write("\n")
    }

    private fun encodeString(writer: Writer, value: String) {
        writer.write("\"${escapeQuotes(value)}\"")
    }

    private fun encodeList(writer: Writer, list: List<*>) {
        writer.write("[")

        list.forEachIndexed { index, item ->
            if (index > 0) {
                writer.write(", ")
            }

            when (item) {
                is String -> encodeString(writer, item)
                else -> writer.write(item.toString())
            }
        }

        writer.write("]")
    }

    private fun encodeMap(writer: Writer, map: Map<*, *>) {
        writer.write("{")

        map.entries.forEachIndexed { index, (key, value) ->
            if (index > 0) {
                writer.write(", ")
            }

            // Write key
            if (key is String) {
                if (needsQuotes(key)) {
                    writer.write("\"${escapeQuotes(key)}\"")
                } else {
                    writer.write(key)
                }
            } else {
                writer.write(key.toString())
            }

            writer.write(" = ")

            // Write value
            when (value) {
                is String -> encodeString(writer, value)
                else -> writer.write(value.toString())
            }
        }

        writer.write("}")
    }

    private fun needsQuotes(str: String): Boolean {
        return str.isEmpty() ||
                str.contains(' ') ||
                str.contains('\t') ||
                str.contains('=') ||
                str.contains('[') ||
                str.contains(']') ||
                str.contains('{') ||
                str.contains('}') ||
                str.contains(',') ||
                str.contains('\n') ||
                str.contains('\r') ||
                str.contains('#')
    }

    private fun escapeQuotes(str: String): String {
        return str.replace("\"", "\\\"")
    }
}

// Helper function to create a new INI config and populate it
fun createIniConfig(): IniConfig {
    val config = IniConfig()

    // Add some values
    config.set("server", "host", "localhost")
    config.set("server", "port", 8080L)
    config.set("server", "debug", true)

    // Add a list
    config.set("users", "admins", listOf("admin", "root", "superuser"))

    // Add a map
    config.set("database", "credentials", mapOf(
        "username" to "dbuser",
        "password" to "dbpass",
        "host" to "db.example.com"
    ))

    // Add a section with inheritance
    config.set("server.http", "enabled", true)
    config.set("server.http", "port", 80L)

    return config
}

// Usage example
fun main() {
    // Create a new config
    val config = createIniConfig()
    val encoder = ConfigWriter()

    val start = System.nanoTime()
    // Write to file
    encoder.encode(File("./output.ini"), config)
    println("Took ${System.nanoTime() - start}ns")

    // Write with comments
    val comments = mapOf(
        "" to "Configuration file for the application",
        "server.host" to "The hostname to bind to",
        "server.port" to "The port to listen on",
        "database.credentials" to "Database connection credentials"
    )

    val sectionComments = mapOf(
        "server" to "Server configuration",
        "users" to "User management",
        "database" to "Database settings"
    )

    // Read the file back
    val readConfig = IniConfig()
    val parser = ConfigReader.IniParser(readConfig)
    parser.parse(File("./output.ini"))
}