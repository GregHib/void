package world.gregs.config

import java.io.*

object Groml {
    private val writer = ConfigWriter()

    fun decodeFromString(string: String): Map<String, Any> {
        val api = ConfigMap()
        BufferedInputStream(string.byteInputStream()).use { input ->
            api.parse(input)
        }
        return api.sections
    }

    fun decodeFromFile(path: String): Map<String, Any> {
        val api = ConfigMap()
        BufferedInputStream(FileInputStream(path)).use { input ->
            api.parse(input)
        }
        return api.sections
    }

    fun encodeToString(map: Map<String, Any>): String {
        val stringWriter = StringWriter()
        BufferedWriter(stringWriter).use { output ->
            writer.encode(output, map)
        }
        return stringWriter.buffer.toString()
    }

    fun encodeToFile(map: Map<String, Any>, path: String) {
        BufferedWriter(FileWriter(path)).use { output ->
            writer.encode(output, map)
        }
    }
}