@file:Suppress("UNCHECKED_CAST")

package world.gregs.toml.read

import java.io.BufferedWriter
import java.time.temporal.Temporal
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import java.time.LocalDateTime
import java.time.LocalDate

class TomlWriter {

    /**
     * Writes a Map to a BufferedWriter in TOML format.
     *
     * @param writer The BufferedWriter to write to
     * @param map The map to convert to TOML
     */
    fun write(writer: BufferedWriter, map: Map<String, Any>) {
        writeMap(writer, map, null)
    }

    /**
     * Recursively writes a map to the writer with the given path prefix.
     */
    private fun writeMap(writer: BufferedWriter, map: Map<String, Any>, path: String?) {
        var variableHeader = false
        for ((key, value) in map) {
            if (value !is Map<*, *>) {
                // Only write table header if we have a path, and it's not the root
                if (!variableHeader && path != null) {
                    variableHeader = true
                    writer.write('['.code)
                    writer.write(path)
                    writer.write("]\n")
                }

                writer.write(key)
                writer.write(" = ")
                writeValue(writer, value)
                writer.write('\n'.code)
            }
        }

        // Add a newline if we're going to write nested maps next
        if (variableHeader || path == null) {
            writer.write('\n'.code)
        }

        // Write nested maps with updated paths
        for ((key, value) in map) {
            if (value is Map<*, *>) {
                val newPath = if (path == null) key else "$path.$key"
                writeMap(writer, value as MutableMap<String, Any>, newPath)
            }
        }
    }

    /**
     * Writes a value directly to the writer according to TOML specification.
     */
    private fun writeValue(writer: BufferedWriter, value: Any?) {
        when (value) {
            null -> writer.write("null")
            is Boolean -> writer.write(value.toString())
            is Number -> writer.write(value.toString())
            is String -> writeEscapedString(writer, value)
            is Temporal -> writeDate(writer, value)
            is Map<*, *> -> {
                writeInlineMap(writer, value as Map<String, Any>)
            }
            is List<*> -> writeArray(writer, value)
            is Array<*> -> writeArray(writer, value.toList())
            else -> writeEscapedString(writer, value.toString())
        }
    }

    /**
     * Writes an escaped string directly to the writer.
     */
    private fun writeEscapedString(writer: BufferedWriter, value: String) {
        writer.write('"'.code)
        for (char in value) {
            when (char) {
                '\\' -> writer.write("\\\\")
                '\"' -> writer.write("\\\"")
                '\b' -> writer.write("\\b")
                '\t' -> writer.write("\\t")
                '\n' -> writer.write("\\n")
                '\r' -> writer.write("\\r")
                '\u000C' -> writer.write("\\f")
                else -> writer.write(char.code)
            }
        }
        writer.write('"'.code)
    }

    /**
     * Writes a date directly to the writer according to TOML specification.
     */
    private fun writeDate(writer: BufferedWriter, value: Temporal) {
        writer.write('"'.code)
        when (value) {
            is ZonedDateTime -> writer.write(value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            is LocalDateTime -> writer.write(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            is LocalDate -> writer.write(value.format(DateTimeFormatter.ISO_LOCAL_DATE))
            else -> writer.write(value.toString())
        }
        writer.write('"'.code)
    }

    /**
     * Writes a map as an inline TOML table directly to the writer.
     */
    private fun writeInlineMap(writer: BufferedWriter, map: Map<String, Any>) {
        if (map.isEmpty()) {
            writer.write("{ }")
            return
        }

        writer.write("{ ")
        var first = true
        for ((k, v) in map) {
            if (!first) {
                writer.write(", ")
            }
            writer.write(k)
            writer.write(" = ")
            writeValue(writer, v)
            first = false
        }
        writer.write(" }")
    }

    /**
     * Writes an array directly to the writer according to TOML specification.
     */
    private fun writeArray(writer: BufferedWriter, array: List<*>) {
        if (array.isEmpty()) {
            writer.write("[]")
            return
        }

        // Use multi-line format for complex elements like maps or nested arrays
        val hasComplexElements = array.any { it is Map<*, *> || it is List<*> || it is Array<*> }

        if (hasComplexElements) {
            writer.write("[\n  ")
            var first = true
            for (item in array) {
                if (!first) {
                    writer.write(",\n  ")
                }
                writeValue(writer, item)
                first = false
            }
            writer.write("\n]")
        } else {
            writer.write("[")
            var first = true
            for (item in array) {
                if (!first) {
                    writer.write(", ")
                }
                writeValue(writer, item)
                first = false
            }
            writer.write("]")
        }
    }
}