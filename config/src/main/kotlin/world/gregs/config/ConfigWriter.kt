package world.gregs.config

import java.io.Writer

class ConfigWriter {

    fun encode(writer: Writer, map: Map<String, Any>) {
        encodeSection(writer, "", map)
    }

    @Suppress("UNCHECKED_CAST")
    private fun encodeSection(writer: Writer, section: String, map: Map<String, Any>) {
        var start = true
        for ((key, value) in map) {
            if (value is Map<*, *>) {
                if (!start) {
                    writer.write("\n")
                    writer.flush()
                }
                encodeSection(writer, if (section.isBlank()) key else "${section}.${key}", value as Map<String, Any>)
                start = true
            } else {
                if (start) {
                    if (section.isNotBlank()) {
                        writer.write("[$section]\n")
                    }
                    start = false
                }
                encodeKeyValue(writer, key, value)
            }
        }

        // Add a blank line after each section
        if (!start) {
            writer.write("\n")
            writer.flush()
        }
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
        encodeValue(writer, value)

        writer.write("\n")
    }

    private fun encodeValue(writer: Writer, value: Any?) {
        when (value) {
            is String -> encodeString(writer, value)
            is Long, is Int -> writer.write(value.toString())
            is Double, is Float -> writer.write(value.toString())
            is Boolean -> writer.write(value.toString())
            is List<*> -> encodeList(writer, value)
            is Array<*> -> encodeArray(writer, value)
            is Map<*, *> -> encodeMap(writer, value)
            null -> writer.write("null")
            else -> writer.write(value.toString())
        }
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

            encodeValue(writer, item)
        }

        writer.write("]")
    }

    private fun encodeArray(writer: Writer, list: Array<*>) {
        writer.write("[")

        for ((index, item) in list.withIndex()) {
            if (index > 0) {
                writer.write(", ")
            }

            encodeValue(writer, item)
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
            encodeValue(writer, value)
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