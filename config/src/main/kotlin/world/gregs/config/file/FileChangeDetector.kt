package world.gregs.config.file

import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.pathString

class FileChangeDetector {

    fun detect(data: File, temp: File, forceClear: Boolean = false) = detect(
        data.resolve("dirs.txt").readLines().map { data.resolve(it).toPath() }.toSet(), temp, forceClear
    )

    /**
     * Detects changes in the directory tree.
     * Returns modified, added, and removed files.
     */
    fun detect(directories: Set<Path>, temp: File, forceClear: Boolean = false): FileChanges {
        temp.mkdirs()
        val hashFile = temp.resolve("hashes.dat")
        var previousHashes = loadHashes(hashFile)
        val currentFiles = collectFiles(directories)
        val currentHashes = computeHashes(currentFiles)
        var incremental = previousHashes.isNotEmpty()
        if (forceClear) {
            previousHashes = emptyMap()
            incremental = false
        }

        val modified = mutableSetOf<Path>()
        val added = mutableSetOf<Path>()
        val removed = mutableSetOf<Path>()

        // Find modified and added files
        for ((file, hash) in currentHashes) {
            val previousHash = previousHashes[file]
            when {
                previousHash == null -> added.add(file)
                previousHash != hash -> modified.add(file)
            }
        }

        // Find removed files
        for (file in previousHashes.keys) {
            if (file !in currentHashes) {
                removed.add(file)
            }
        }

        saveHashes(hashFile, currentHashes)

        return FileChanges(
            modified = modified,
            added = added,
            removed = removed,
            incremental = incremental
        )
    }

    /**
     * Recursively collects all files in the directory tree.
     */
    private fun collectFiles(directories: Set<Path>): Set<Path> {
        val set = mutableSetOf<Path>()
        for (dir in directories) {
            if (!dir.exists()) {
                continue
            }
            for (path in Files.walk(dir)) {
                if (!path.isRegularFile()) {
                    continue
                }
                set.add(path)
            }
        }
        return set
    }

    /**
     * Computes SHA-256 hashes for all files.
     */
    private fun computeHashes(files: Set<Path>): Map<Path, String> {
        return files.associateWith { file ->
            computeFileHash(file)
        }
    }

    /**
     * Computes SHA-256 hash of a single file.
     */
    private fun computeFileHash(file: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)

        file.inputStream().use { input ->
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun saveHashes(file: File, hashes: Map<Path, String>) {
        val writer = ArrayWriter(5_000_00)
        writer.writeInt(hashes.size)
        for ((path, hash) in hashes) {
            writer.writeString(path.pathString)
            writer.writeString(hash)
        }
        file.writeBytes(writer.toArray())
    }

    private fun loadHashes(file: File): Map<Path, String> {
        if (!file.exists()) {
            return emptyMap()
        }
        val reader = ArrayReader(file.readBytes())
        val size = reader.readInt()
        val map = mutableMapOf<Path, String>()
        for (i in 0 until size) {
            map[Path.of(reader.readString())] = reader.readString()
        }
        return map
    }
}