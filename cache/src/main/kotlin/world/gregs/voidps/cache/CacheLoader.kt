package world.gregs.voidps.cache

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader
import java.io.File
val logger = InlineLogger()
inline fun <reified T : Definition> loadLive(directory: File, decoder: DefinitionDecoder<T>): Array<T> {
    val start = System.currentTimeMillis()
    val reader = BufferReader(directory.resolve("index${decoder.index}.dat").readBytes())
    val size = reader.readInt()
    val array = Array(size) { decoder.create(it) }
    while (reader.position() < reader.length) {
        val id = decoder.readId(reader)
        val definition = array[id]
        decoder.readLoop(definition, reader)
        decoder.changeDefValues(definition)
    }
    println("Loaded $size ${T::class.simpleName} in ${System.currentTimeMillis() - start}ms")
    return array
}

inline fun <reified T : Definition> loadCache(cache: Cache, decoder: DefinitionDecoder<T>): Array<T> {
    val start = System.currentTimeMillis()
    val size = decoder.size(cache)
    val array = Array(size + 1) { decoder.create(it) }
    for (archiveId in cache.getArchives(decoder.index)) {
        val files = cache.getArchiveData(decoder.index, archiveId) ?: continue
        for ((fileId, file) in files) {
            if (file == null) {
                continue
            }
            val id = decoder.id(archiveId, fileId)
            val definition = array[id]
            decoder.readLoop(definition, BufferReader(file))
            decoder.changeDefValues(definition)
        }
    }
    println("Loaded $size ${T::class.simpleName} in ${System.currentTimeMillis() - start}ms")
    return array
}