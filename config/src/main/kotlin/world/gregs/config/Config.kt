package world.gregs.config

import java.io.*

/**
 * Methods for reading and writing toml configuration files
 */
object Config {
    private val writer = ConfigWriter()

    /**
     * Convenience helper for reading toml strings as a map of sections, for performance use [stringReader]
     */
    fun decodeFromString(string: String): Map<String, Any> {
        return BufferedInputStream(string.byteInputStream()).use { input ->
            val api = ConfigReader(input)
            api.sections()
        }
    }

    /**
     * Convenience helper for reading toml files as a map of sections, for performance use [fileReader]
     */
    fun decodeFromFile(path: String): Map<String, Any> {
        return BufferedInputStream(FileInputStream(path)).use { input ->
            val api = ConfigReader(input)
            api.sections()
        }
    }

    /**
     * Make sure to call [ConfigReader.close] after finished
     */
    fun fileReader(path: String): ConfigReader {
        return ConfigReader(BufferedInputStream(FileInputStream(path)))
    }

    /**
     * Make sure to call [ConfigReader.close] after finished
     */
    fun fileReader(file: File): ConfigReader {
        return ConfigReader(BufferedInputStream(FileInputStream(file)))
    }

    /**
     * Make sure to call [ConfigReader.close] after finished
     */
    fun stringReader(string: String): ConfigReader {
        return ConfigReader(BufferedInputStream(string.byteInputStream()))
    }

    fun decodeFromFile(path: String, api: ConfigReader) {
        BufferedInputStream(FileInputStream(path)).use { input ->
            api.parse(input)
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