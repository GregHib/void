package world.gregs.toml

import world.gregs.toml.read.*
import java.io.*

object Toml {
    private val tomlReader = TomlStream()
    private val tomlWriter = TomlWriter()

    fun decodeFromFile(path: String, maximumStringSize: Int = 1024, maximumNesting: Int = 10): Map<String, Any> {
        val api = TomlMapApi()
        val buffer = ByteArray(maximumStringSize)
        val addressBuffer = Array<Any>(maximumNesting) { "" }
        BufferedInputStream(FileInputStream(path)).use { bis ->
            decodeFromStream(bis, api, buffer, addressBuffer)
        }
        return api.root
    }

    fun decodeFromStream(stream: BufferedInputStream, api: TomlStream.Api, buffer: ByteArray, addressBuffer: Array<Any>) {
        tomlReader.read(stream, api, buffer, addressBuffer)
    }

    fun decodeFromString(string: String, maximumStringSize: Int = 1024, maximumNesting: Int = 10): Map<String, Any> {
        val api = TomlMapApi()
        val buffer = ByteArray(maximumStringSize)
        val addressBuffer = Array<Any>(maximumNesting) { "" }
        BufferedInputStream(string.byteInputStream()).use { reader ->
            decodeFromStream(reader, api, buffer, addressBuffer)
        }
        return api.root
    }

    fun encodeToString(map: Map<String, Any>): String {
        val stringWriter = StringWriter()
        encodeToWriter(map, BufferedWriter(stringWriter))
        return stringWriter.buffer.toString()
    }

    fun encodeToFile(map: Map<String, Any>, path: String) {
        encodeToWriter(map, BufferedWriter(FileWriter(path)))
    }

    fun encodeToWriter(map: Map<String, Any>, writer: BufferedWriter) {
        writer.use { buffer ->
            tomlWriter.write(buffer, map)
        }
    }

}