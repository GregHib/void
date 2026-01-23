package world.gregs.config.param

import world.gregs.config.Config
import world.gregs.config.file.FileCodec
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import java.io.File
import java.nio.file.Path
import kotlin.io.path.pathString

class SpawnFileCodec(
    name: String,
    val codec: Parameters,
    val maxStringSize: Int = 100,
    val maxBufferSize: Int = 1_000_00,
) : FileCodec {

    val name: String = "${name}_spawns"

    override fun added(base: File, added: List<Path>) {
        val ids = readTiles(base)
        for (path in added) {
        // TODO can't do validation if it's shared with gradle.
            Config.fileReader(path.pathString, maxStringSize) {
                while (nextPair()) {
                    require(key() == "spawns")
                    while (nextElement()) {
                        var id = ""
                        var direction = ""
                        var x =0
                        var y = 0
                        var level = 0
                        while (nextEntry()) {
                            when (val key = key()) {
                                "id" -> id = string()
                                "x" -> x = int()
                                "y" -> y = int()
                                "level" -> level = int()
                                "direction" -> direction = string()
                            }
                        }
                    }
                }
            }
            // Read all add ids
        }
        writeTiles(base, ids)
    }

    override fun removed(base: File, removed: List<Path>) {

    }

    override fun modified(base: File, modified: List<Path>) {

    }

    private fun readTiles(base: File): MutableMap<String, MutableSet<Int>> {
        val ids = mutableMapOf<String, MutableSet<Int>>()
        val file = base.resolve("${name}.tiles")
        if (!file.exists()) {
            return ids
        }
        val reader = ArrayReader(file.readBytes())
        while (reader.position < reader.length) {
            val set = mutableSetOf<Int>()
            val file = reader.readString()
            val size = reader.readShort()
            for (i in 0 until size) {
                set.add(reader.readInt())
            }
            ids[file] = set
        }
        return ids
    }

    private fun writeTiles(base: File, ids: Map<String, Set<Int>>) {
        val writer = ArrayWriter(maxBufferSize)
        for ((key, value) in ids) {
            writer.writeString(key)
            writer.writeShort(value.size)
            for (id in value) {
                writer.writeInt(id)
            }
        }
        base.resolve("${name}.tiles").writeBytes(writer.toArray())
    }


}