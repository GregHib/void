package world.gregs.toml

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.toml.read.CharReader
import world.gregs.toml.read.TomlReader
import java.io.File

open class Toml(block: Settings.() -> Unit = {}) {

    data class Settings(
        var multiLine: Boolean = true,
        var appendArrays: Boolean = true,
    )

    private val settings = Settings()
    private val reader = CharReader()
    private val tomlReader = TomlReader(reader, settings)

    init {
        block.invoke(settings)
    }

    fun decodeFromFile(path: String): Map<String, Any> {
        val file = File(path)
        val charArray = CharArray(file.length().toInt())
        val length = file.reader().use {
            it.read(charArray)
        }
        return decodeFromCharArray(charArray, length)
    }

    fun decodeFromString(string: String): Map<String, Any> {
        return decodeFromCharArray(string.toCharArray())
    }

    fun decodeFromCharArray(charArray: CharArray, length: Int = charArray.size, root: MutableMap<String, Any> = Object2ObjectOpenHashMap()): Map<String, Any> {
        reader.set(charArray, length)
        reader.nextLine()
        if (!reader.inBounds) {
            return emptyMap()
        }
        return tomlReader.read(root)
    }

    companion object : Toml()
}