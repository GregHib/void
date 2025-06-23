package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import com.displee.compress.CompressionType
import world.gregs.voidps.cache.Index

object RemoveBzip2 {
    fun remove(lib: CacheLibrary) {
        println("Removing slow compression...")
        var indices = 0
        var archives = 0
        val targetType = CompressionType.GZIP
        val compressor = lib.compressors.get(targetType)
        for (index in lib.indices()) {
            if (index.version == 0 || index.id == Index.NATIVE_LIBRARIES) {
                continue
            }
            if (index.compressionType == CompressionType.BZIP2) {
                index.compressionType = targetType
                index.compressor = compressor
                index.flag()
                indices++
            }
            for (archiveId in index.archiveIds()) {
                val archive = index.archive(archiveId) ?: continue
                if (archive.compressionType == CompressionType.BZIP2) {
                    archive.compressionType = targetType
                    archive.compressor = compressor
                    archive.flag()
                    archives++
                }
            }
        }
        if (indices > 0 || archives > 0) {
            lib.update()
        }
        println("Removed old compression from $archives archives and $indices indices.")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val path = "./data/cache/test/"
        val lib = CacheLibrary(path)
        remove(lib)
    }
}
