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
 * A binary copy of the state a loader built, so the cache and config files only need reading again when they change.
 * Valid while the [key] (derived from everything the loader reads) matches the one the snapshot was written with.
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
     * Decodes the snapshot with [block].
     * @return the decoded value, or null if the snapshot wasn't valid or couldn't be decoded.
     */
    fun <T : Any> read(block: (SnapshotReader) -> T): T? {
        val reader = reader() ?: return null
        return try {
            block(reader)
        } catch (e: Exception) {
            logger.warn(e) { "Unable to restore snapshot ${path.name}." }
            null
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
    }
}
