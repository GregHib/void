package world.gregs.config

import java.io.Writer

// Extension of the IniConfig class to include encoding functionality
class ConfigWriter {

    fun encode(writer: Writer, map: Map<String, Any>) {
        val sectionParentMap = buildSectionHierarchy(map)

        // Write sections in an order that ensures parents come before children
        val orderedSections = orderSections(map, sectionParentMap)

        for ((key, value) in map) {
            if (value !is Map<*, *>) {
                encodeKeyValue(writer, key, value)
            }
        }

        for (section in orderedSections) {
            // Write section header
            writer.write("[$section]\n")

            // Write section contents
            val m = map[section]
            if (m is Map<*, *>) {
                for ((key, value) in m as Map<String, Any>) {
                    encodeKeyValue(writer, key, value)
                }
            }

            // Add a blank line after each section
            writer.write("\n")
        }
    }

    private fun orderSections(map: Map<String, Any>, sectionParentMap: Map<String, String?>): List<String> {
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
        for ((key, value) in map) {
            if (value is Map<*, *>) {
                visit(key)
            }
        }
        return result
    }

    private fun buildSectionHierarchy(map: Map<String, Any>): Map<String, String?> {
        val result = mutableMapOf<String, String?>()

        for (section in map.keys) {
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

        for ((index, item) in list.withIndex()) {
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

        for ((index, pair) in map.entries.withIndex()) {
            val (key, value) = pair
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