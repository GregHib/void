import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.jetbrains.kotlin.it.unimi.dsi.fastutil.objects.ba
import world.gregs.config.Config
import world.gregs.config.param.NpcParams
import world.gregs.config.param.Parameters
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import java.io.File

/**
 * Gradle task which incrementally tracks config files inside a given directory.
 */
abstract class ConfigMetadataTask : DefaultTask() {

    @get:Incremental
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val directories: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    init {
        description = ""
        group = "metadata"
    }

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val base = output.get().asFile
        base.mkdirs()
        var count = 0

        val ids = readIds(base)
        if (!inputChanges.isIncremental) {
            val changes = mutableMapOf<String, MutableList<String>>()
            for (file in directories) {
                val ending = file.name.substringAfter(".")
                println("Change $ending ${file.path}")
                changes.getOrPut(ending) { mutableListOf() }.add(file.path)
                count++
            }
            // Write fully to files
            for ((type, files) in changes) {
                when (type) {
                    "npcs.toml" -> {
                        val configs = mutableMapOf<Int, MutableMap<Int, Any>>()
                        for (file in files) {
                            for (values in readConfig(file, NpcParams)) {
                                val id = values[Parameters.ID] as Int
                                configs[id] = values
                                ids.getOrPut(file) { mutableListOf() }.add(id)
                            }
                        }
                        val writer = ArrayWriter(1_000_000)
                        val indices = IntArray(MAX_ID)
                        for (i in 0 until MAX_ID) {
                            val values = configs[i] ?: continue
                            writeConfig(writer, values, indices, NpcParams)
                        }
                        writeData(writer, base, "npcs")
                        writeIndices(indices, base, "npcs")
                    }
                }
            }
        } else {
            val updates = mutableMapOf<String, MutableList<String>>()
            val removals = mutableMapOf<String, MutableList<String>>()
            for (change in inputChanges.getFileChanges(directories)) {
                val ending = change.file.name.substringAfter(".")
                println("Update ${change.changeType.name} $ending ${change.file.path}")
                if (change.changeType == ChangeType.REMOVED) {
                    removals.getOrPut(ending) { mutableListOf() }.add(change.file.path)
                } else {
                    updates.getOrPut(ending) { mutableListOf() }.add(change.file.path)
                }
                count++
            }
            // Removals
            for ((type, files) in removals) {
                when (type) {
                    "npcs.toml" -> {
                        val reader = readData(base, "npcs")
                        var writer = ArrayWriter(buffer = reader.array)
                        val indices = readIndices(base, "npcs")
                        for (file in files) {
                            for (id in ids[file] ?: continue) {
                                if (indices[id] == 0) {
                                    continue // Already empty
                                }
                                val index = indices.startingIndex(id)
                                indices[id] = 0
                                val size = indices.size(id, reader.length)
                                writer = writer.cut(index, size)
                                ids[file]?.remove(id)
                            }
                        }
                        writeData(writer, base, "npcs")
                        writeIndices(indices, base, "npcs")
                    }
                }
            }
            // Additions and modifications
            for ((type, files) in updates) {
                when (type) {
                    "npcs.toml" -> {
                        val configs = mutableMapOf<Int, MutableMap<Int, Any>>()
                        val indices = readIndices(base, "npcs")
                        val reader = readData(base, "npcs")
                        for (file in files) {
                            for (values in readConfig(file, NpcParams)) {
                                val id = values[Parameters.ID] as Int
                                ids.getOrPut(file) { mutableListOf() }.add(id)
                                configs[id] = values
                            }
                        }
                        val encoded = mutableMapOf<Int, ByteArray>()
                        for ((id, params) in configs) {
                            val writer = ArrayWriter(500_000)
                            writeConfig(writer, params, indices, NpcParams)
                            encoded[id] = writer.toArray()
                        }
                        var writer = ArrayWriter(buffer = reader.array)
                        for ((id, params) in encoded) {
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
                            for (i in id until MAX_ID) {
                                indices[i] = indices[i] + difference
                            }
                        }
                        writeData(writer, base, "npcs")
                        writeIndices(indices, base, "npcs")
                    }
                }
            }
        }
        writeIds(base, ids)
    }

    private fun writeIds(base: File, ids: Map<String, List<Int>>) {
        val writer = ArrayWriter(1_000_000)
        writer.writeInt(ids.size)
        for ((file, ids) in ids) {
            writer.writeString(file)
            writer.writeInt(ids.size)
            writer.writeBytes(ids.toIntArray())
        }
        base.resolve("ids.dat").writeBytes(writer.toArray())
    }

    private fun readIds(base: File): MutableMap<String, MutableList<Int>> {
        val ids = mutableMapOf<String, MutableList<Int>>()
        val file = base.resolve("ids.dat")
        if (file.exists()) {
            val reader = ArrayReader(file.readBytes())
            val size = reader.readInt()
            for (i in 0 until size) {
                val file = reader.readString()
                val array = IntArray(reader.readInt())
                reader.readBytes(array)
                ids[file] = array.toMutableList()
            }
        }
        return ids
    }

    private fun writeData(writer: ArrayWriter, base: File, name: String) {
        base.resolve("${name}.dat").writeBytes(writer.toArray())
    }

    private fun writeIndices(indices: IntArray, base: File, name: String) {
        val writer = ArrayWriter(indices.size * 4)
        writer.writeBytes(indices)
        base.resolve("${name}.idx").writeBytes(writer.array())
    }

    private fun readData(base: File, name: String): ArrayReader = ArrayReader(base.resolve("${name}.dat").readBytes())

    private fun readIndices(base: File, name: String): IntArray {
        val indices = IntArray(MAX_ID)
        ArrayReader(base.resolve("${name}.idx").readBytes()).readBytes(indices)
        return indices
    }

    private fun writeConfig(writer: ArrayWriter, values: MutableMap<Int, Any>, indices: IntArray, params: Parameters) {
        val id = values[Parameters.ID] as Int
        val stringId = values[Parameters.STRING_ID] as String
        writer.writeInt(id)
        indices[id] = writer.position()
        writer.writeString(stringId)
        params.write(writer, values)
    }

    private fun readConfig(file: String, params: Parameters, stringSize: Int = 250): List<MutableMap<Int, Any>> {
        val list = mutableListOf<MutableMap<Int, Any>>()
        Config.fileReader(file, stringSize) {
            while (nextSection()) {
                val section = section()
                val values = params.read(this)
                values[Parameters.STRING_ID] = section
                list.add(values)
            }
        }
        return list
    }

    fun IntArray.startingIndex(id: Int) = this[id] - 4 // For the id header

    fun IntArray.size(id: Int, end: Int): Int {
        val endIndex = this.getOrNull(id + 1)?.let { it - 4 } ?: end
        return endIndex - this[id]
    }

    companion object {
        const val MAX_ID = 80_000
    }
}
