package world.gregs.config

import java.io.*

object Config {
    private val writer = ConfigWriter()

    fun decodeFromString(string: String): Map<String, Any> {
        return BufferedInputStream(string.byteInputStream()).use { input ->
            val api = ConfigReader(input)
            api.sections()
        }
    }

    fun decodeFromFile(path: String): Map<String, Any> {
        return BufferedInputStream(FileInputStream(path)).use { input ->
            val api = ConfigReader(input)
            api.sections()
        }
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