package world.gregs.voidps.cache.memory

import com.displee.cache.CacheLibrary
import com.displee.compress.CompressionType

class RemoveBzip2 {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val path = "./data/cache/test/"
            val lib = CacheLibrary(path)
            var indices = 0
            var archives = 0
            for (index in lib.indices()) {
                if (index.version == 0) {
                    continue
                }
                if (index.compressionType == CompressionType.BZIP2) {
                    index.compressionType = CompressionType.GZIP
                    index.flag()
                    indices++
                }
                for (archive in index.archives()) {
                    for (file in archive.files) {
                        lib.data(index.id, archive.id, file.key)
                    }
                    if (archive.compressionType == CompressionType.BZIP2) {
                        archive.compressionType = CompressionType.GZIP
                        archive.flag()
                        archives++
                    }
                }
            }
            lib.update()
            println("Recompressed $archives archives and $indices indices")
        }
    }
}