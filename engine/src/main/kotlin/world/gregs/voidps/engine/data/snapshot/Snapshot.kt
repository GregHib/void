package world.gregs.voidps.engine.data.snapshot

import com.github.michaelbull.logging.InlineLogger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

/**
 * A binary copy of the final result of parsing a set of config files, so the files only need parsing again when they change.
 * Valid while the [key] (derived from the files' sizes and modified times) matches the one the snapshot was written with.
 */
class Snapshot(private val path: Path, private val key: Long) {

    /**
     * @return a reader positioned after the header, or null if there's no valid snapshot for the current [key].
     */
    fun reader(): SnapshotReader? {
        if (!path.exists()) {
            return null
        }
        return try {
            val reader = SnapshotReader(path.readBytes())
            if (reader.int() == MAGIC && reader.long() == key) reader else null
        } catch (e: Exception) {
            logger.warn(e) { "Unable to read snapshot ${path.name}." }
            null
        }
    }

    /**
     * Decodes the snapshot with [block], which should only apply what it decodes once reading has completed.
     * @return true if the snapshot was valid and [block] completed.
     */
    fun restore(block: (SnapshotReader) -> Unit): Boolean {
        val reader = reader() ?: return false
        return try {
            block(reader)
            true
        } catch (e: Exception) {
            logger.warn(e) { "Unable to restore snapshot ${path.name}." }
            false
        }
    }

    fun write(block: SnapshotWriter.() -> Unit) {
        val temp = path.resolveSibling("${path.name}.${ProcessHandle.current().pid()}.tmp")
        try {
            val writer = SnapshotWriter()
            writer.int(MAGIC)
            writer.long(key)
            writer.block()
            path.createParentDirectories()
            temp.writeBytes(writer.toByteArray())
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: Exception) {
            logger.warn(e) { "Unable to write snapshot ${path.name}." }
            temp.deleteIfExists()
            path.deleteIfExists()
        }
    }

    companion object {
        private val logger = InlineLogger()
        private const val MAGIC = 0x564F4944

        /** Increment when the encoding or the content of any snapshot changes. */
        const val VERSION = 1
    }
}

/**
 * Restores from the snapshot if it's valid, otherwise [load]s and then saves it.
 */
inline fun Snapshot?.restoreOrLoad(restore: (Snapshot) -> Boolean, save: (Snapshot) -> Unit, load: () -> Unit) {
    if (this != null && restore(this)) {
        return
    }
    load()
    if (this != null) {
        save(this)
    }
}
