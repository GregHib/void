package world.gregs.toml

import world.gregs.toml.read.*
import java.io.*

object Toml {
    private val tomlReader = TomlStream()

    fun decodeFromFile(path: String, maximumStringSize: Int = 1024, maximumNesting: Int = 10): Map<String, Any> {
        val api = TomlMapApi()
        val buffer = ByteArray(maximumStringSize)
        val addressBuffer = Array(maximumNesting) { "" }
        BufferedInputStream(FileInputStream(path)).use { bis ->
            decodeFromStream(bis, api, buffer, addressBuffer)
        }
        return api.root
    }

    fun decodeFromStream(stream: BufferedInputStream, api: TomlStream.Api, buffer: ByteArray, addressBuffer: Array<String>) {
        tomlReader.read(stream, api, buffer, addressBuffer)
    }

    fun decodeFromString(string: String, maximumStringSize: Int = 1024, maximumNesting: Int = 10): Map<String, Any> {
        val api = TomlMapApi()
        val buffer = ByteArray(maximumStringSize)
        val addressBuffer = Array(maximumNesting) { "" }
        BufferedInputStream(string.byteInputStream()).use { reader ->
            decodeFromStream(reader, api, buffer, addressBuffer)
        }
        return api.root
    }

}