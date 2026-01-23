package world.gregs.config.param

import world.gregs.config.Config
import world.gregs.config.file.FileCodec
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import java.io.File
import java.nio.file.Path
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.set
import kotlin.io.path.pathString

class NormalFileCodec(
    name: String,
    val codec: Parameters,
    val maxSize: Int,
    val maxStringSize: Int = 100,
    val maxBufferSize: Int = 1_000_00,
) : FileCodec {

    val name: String = "${name}_configs"

    override fun added(base: File, added: List<Path>) {
        val configs = mutableMapOf<Int, MutableMap<Int, Any>>()
        val ids = readIds(base)
        for (path in added) {
            for (values in readConfig(path.pathString)) {
                val id = values[Params.ID] as Int
                configs[id] = values
                ids.getOrPut(path.pathString) { mutableSetOf() }.add(id)
            }
        }
        val writer = ArrayWriter(maxBufferSize)
        val indices = readIndices(base)
        for (i in indices.indices) { // Reorder
            val values = configs[i] ?: continue
            writeConfig(writer, values, indices)
        }
        writeIds(base, ids)
        writeData(writer, base)
        writeIndices(indices, base)
    }

    override fun modified(base: File, modified: List<Path>) {
        val configs = mutableMapOf<Int, MutableMap<Int, Any>>()
        val indices = readIndices(base)
        val reader = readData(base)
        val ids = readIds(base)
        for (path in modified) {
            // TODO don't edits need to remove any ids that were removed?
            for (values in readConfig(path.pathString)) {
                val id = values[Params.ID] as Int
                ids.getOrPut(path.pathString) { mutableSetOf() }.add(id)
                configs[id] = values
            }
        }
        var writer = ArrayWriter(buffer = reader.array)
        val temp = ArrayWriter(maxBufferSize)
        for ((id, values) in configs) {
            temp.clear()
            writeConfig(temp, values, indices)
            val params = temp.toArray()
            if (indices[id] == 0) {
                // Append
                indices[id] = writer.position() + 4
                writer.writeBytes(params)
                continue
            }
            val start = indices.startingIndex(id)
            val currentSize = indices.size(id, reader.length)
            val newSize = params.size
            val difference = newSize - currentSize
            // Adjust array
            if (currentSize > newSize) {
                writer = writer.insert(start, currentSize - newSize)
            } else if (currentSize < newSize) {
                writer = writer.cut(start, newSize - currentSize)
            }
            // Overwrite
            writer.writeBytes(params, start, params.size)
            for (i in indices.indices) {
                indices[i] = indices[i] + difference
            }
        }
        writeIds(base, ids)
        writeData(writer, base)
        writeIndices(indices, base)
    }

    override fun removed(base: File, removed: List<Path>) {
        val reader = readData(base)
        var writer = ArrayWriter(buffer = reader.array)
        val indices = readIndices(base)
        val ids = readIds(base)
        for (path in removed) {
            for (id in ids[path.pathString] ?: continue) {
                if (indices[id] == 0) {
                    continue // Already empty
                }
                val index = indices.startingIndex(id)
                indices[id] = 0
                val size = indices.size(id, reader.length)
                writer = writer.cut(index, size)
                ids[path.pathString]?.remove(id)
            }
        }
        writeIds(base, ids)
        writeData(writer, base)
        writeIndices(indices, base)
    }

    private fun IntArray.startingIndex(id: Int) = this[id] - 4 // For the id header

    private fun IntArray.size(id: Int, end: Int): Int {
        val endIndex = this.getOrNull(id + 1)?.let { it - 4 } ?: end
        return endIndex - this[id]
    }

    private fun writeData(writer: ArrayWriter, base: File) {
        base.resolve("${name}.dat").writeBytes(writer.toArray())
    }

    private fun writeIndices(indices: IntArray, base: File) {
        val writer = ArrayWriter(indices.size * 4)
        writer.writeBytes(indices)
        base.resolve("${name}.idx").writeBytes(writer.array())
    }

    private fun readData(base: File): ArrayReader = ArrayReader(base.resolve("${name}.dat").readBytes())

    private fun readIds(base: File): MutableMap<String, MutableSet<Int>> {
        val ids = mutableMapOf<String, MutableSet<Int>>()
        val file = base.resolve("${name}.ids")
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

    private fun writeIds(base: File, ids: Map<String, Set<Int>>) {
        val writer = ArrayWriter(maxBufferSize)
        for ((key, value) in ids) {
            writer.writeString(key)
            writer.writeShort(value.size)
            for (id in value) {
                writer.writeInt(id)
            }
        }
        base.resolve("${name}.ids").writeBytes(writer.toArray())
    }

    private fun readIndices(base: File): IntArray {
        val indices = IntArray(maxSize)
        val file = base.resolve("${name}.idx")
        if (!file.exists()) {
            return indices
        }
        ArrayReader(file.readBytes()).readBytes(indices)
        return indices
    }

    private fun writeConfig(writer: ArrayWriter, values: MutableMap<Int, Any>, indices: IntArray) {
        val id = values[Params.ID] as Int
        val stringId = values[Params.STRING_ID] as String
        writer.writeInt(id)
        indices[id] = writer.position()
        writer.writeString(stringId)
        codec.write(writer, values)
    }

    private fun readConfig(file: String): List<MutableMap<Int, Any>> {
        val list = mutableListOf<MutableMap<Int, Any>>()
        Config.fileReader(file, maxStringSize) {
            while (nextSection()) {
                val section = section()
                val values = codec.read(this)
                values[Params.STRING_ID] = section
                list.add(values)
            }
        }
        return list
    }

}