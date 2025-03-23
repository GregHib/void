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

    fun fileReader(path: String, maxStringLength: Int = 100, block: ConfigReader.() -> Unit) {
        ConfigReader(BufferedInputStream(FileInputStream(path)), maxStringLength).use(block)
    }

    fun fileReader(file: File, maxStringLength: Int = 100, block: ConfigReader.() -> Unit) {
        ConfigReader(BufferedInputStream(FileInputStream(file)), maxStringLength).use(block)
    }

    fun stringReader(string: String, maxStringLength: Int = 100, block: ConfigReader.() -> Unit) {
        ConfigReader(BufferedInputStream(string.byteInputStream()), maxStringLength).use(block)
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

    fun fileWriter(path: String, block: Writer.() -> Unit) {
        BufferedWriter(FileWriter(path)).use { output ->
            block.invoke(output)
        }
    }

    fun fileWriter(file: File, block: Writer.() -> Unit) {
        BufferedWriter(FileWriter(file)).use { output ->
            block.invoke(output)
        }
    }
}