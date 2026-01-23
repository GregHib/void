package world.gregs.config

import world.gregs.config.file.FileChanges
import world.gregs.config.file.FileCodec
import world.gregs.config.param.NpcParams
import world.gregs.config.param.NormalFileCodec
import java.io.File
import java.nio.file.Path
import kotlin.io.path.name

object ConfigLoader {
    val codecs = mutableMapOf<String, FileCodec>(
        "npcs.toml" to NormalFileCodec("npc", codec = NpcParams, maxSize = 30_000, maxStringSize = 250, maxBufferSize = 2_000_000)
    )

    fun load(base: File, changes: FileChanges) {
        base.mkdirs()
        val added = changes.added.groupBy { it.name.substringAfter(".") }
        if (changes.incremental) {
            val modified = changes.modified.groupBy { it.name.substringAfter(".") }
            val removed = changes.removed.groupBy { it.name.substringAfter(".") }
            addAll(added, base)
            removeAll(removed, base)
            modifyAll(modified, base)
        } else {
            addAll(added, base)
        }
    }

    private fun addAll(changes: Map<String, List<Path>>, base: File) {
        for ((type, files) in changes) {
            val codec = codecs[type] ?: continue //throw IllegalArgumentException("Unknown file type: $type")
            codec.added(base, files)
        }
    }

    private fun removeAll(changes: Map<String, List<Path>>, base: File) {
        for ((type, files) in changes) {
            val codec = codecs[type] ?: continue //throw IllegalArgumentException("Unknown file type: $type")
            codec.removed(base, files)
        }
    }

    private fun modifyAll(changes: Map<String, List<Path>>, base: File) {
        for ((type, files) in changes) {
            val codec = codecs[type] ?: continue //throw IllegalArgumentException("Unknown file type: $type")
            codec.modified(base, files)
        }
    }

}