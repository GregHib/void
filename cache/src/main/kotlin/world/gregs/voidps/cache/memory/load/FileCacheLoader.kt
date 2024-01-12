package world.gregs.voidps.cache.memory.load

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.memory.cache.FileCache
import java.io.File
import java.io.RandomAccessFile

class FileCacheLoader : CacheLoader {

    override fun load(path: String, mainFile: File, main: RandomAccessFile, index255File: File, index255: RandomAccessFile, indexCount: Int, xteas: Map<Int, IntArray>?, threadUsage: Double): Cache {
        val length = mainFile.length()
        val context = ThreadContext()
        val indices = Array(indexCount) { indexId ->
            val file = File(path, "${Archive.CACHE_FILE_NAME}.idx$indexId")
            if (file.exists()) RandomAccessFile(file, "r") else null
        }
        val cache = FileCache(main, indices, indexCount, xteas)
        for (indexId in 0 until indexCount) {
            cache.readSectorFiles(main, length, index255, indexId, context)
        }
        return cache
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val path = "./data/cache/"

            val memCache = MemoryCacheLoader().load(path)
            val start = System.currentTimeMillis()
            val cache = FileCacheLoader().load(path)
            println("Loaded cache in ${System.currentTimeMillis() - start}ms")

            var count = 0
            for (index in 0 until cache.indexes()) {
                val archives = cache.archives(index) ?: continue
                for (archive in archives) {
                    val files = cache.files(index, archive) ?: continue
                    for (file in files) {
                        val expected = memCache.data(index, archive, file)
                        val actual = cache.data(index, archive, file)
                        if (!expected.contentEquals(actual)) {
                            println("Mismatch $index $archive $file")
                            println("${expected?.take(10)} ${actual?.take(10)}")
                        }
                        count++
                    }
                }
            }
            println("Loaded $count files in ${System.currentTimeMillis() - start}ms")
        }
    }
}