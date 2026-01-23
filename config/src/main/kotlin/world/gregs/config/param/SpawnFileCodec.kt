package world.gregs.config.param

import world.gregs.config.Config
import world.gregs.config.file.FileCodec
import world.gregs.config.param.codec.ParamCodec
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import java.io.File
import java.nio.file.Path
import kotlin.io.path.pathString

class SpawnFileCodec(
    name: String,
    val codecs: Map<String, ParamCodec<*>>,
    val maxStringSize: Int = 100,
    val maxBufferSize: Int = 1_000_00,
) : FileCodec {

    val name: String = "${name}_spawns"

    override fun added(base: File, added: List<Path>) {
        val spawns = readSpawns(base)
        for (path in added) {
            // TODO can't do validation if it's shared with gradle.
            Config.fileReader(path.pathString, maxStringSize) {
                while (nextPair()) {
                    require(key() == "spawns")
                    while (nextElement()) {
                        val array = arrayOfNulls<Any>(2)
                        while (nextEntry()) {
                            val key = key()
//                            codec.codecs[Params.id(key)]?.read(this) ?: continue
                            // This won't work because the structure is diff from Params.
                            val codec = codecs[key] ?: throw IllegalArgumentException("Unknown key: $key")
                            codec.read(this)
//                            when (key) {
//                                "id" -> id = string()
//                                "x" -> x = int()
//                                "y" -> y = int()
//                                "level" -> level = int()
//                                else -> {
//
//                                }
//                                "direction" -> direction = string()
//                                "members" -> members = boolean()
//                            }
                        }
//                        spawns[tile(x, y, level)] = mapOf(
//                            0 to id,
//                            1 to direction,
//                            2 to members,
//                        )
                    }
                }
            }
            // Read all add ids
        }
        writeSpawns(base, spawns)
    }

    fun tile(x: Int, y: Int, level: Int = 0) = (y and 0x3fff) + ((x and 0x3fff) shl 14) + ((level and 0x3) shl 28)


    override fun removed(base: File, removed: List<Path>) {

    }

    override fun modified(base: File, modified: List<Path>) {

    }

    private fun readSpawns(base: File): MutableMap<Int, Map<Int, Any>> {
        val ids = mutableMapOf<Int, Map<Int, Any>>()
        val file = base.resolve("${name}.dat")
        if (!file.exists()) {
            return ids
        }
        val reader = ArrayReader(file.readBytes())
        while (reader.position < reader.length) {
//            ids[reader.readInt()] = codecs.read(reader) ?: continue
        }
        return ids
    }

    private fun writeSpawns(base: File, ids: Map<Int, Map<Int, Any>>) {
        val writer = ArrayWriter(maxBufferSize)
        for ((tile, value) in ids) {
            writer.writeInt(tile)
//            codec.write(writer, value)
        }
        base.resolve("${name}.dat").writeBytes(writer.toArray())
    }


}