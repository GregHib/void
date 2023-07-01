package world.gregs.voidps.cache

import world.gregs.voidps.buffer.read.BufferReader
import java.io.File

inline fun <reified T : Definition> loadLive(directory: File, decoder: DefinitionDecoder<T>): Array<T> {
    val reader = BufferReader(directory.resolve("index${decoder.index}.dat").readBytes())
    val size = reader.readInt()
    val array = Array(size) { decoder.create(it) }
    while (reader.position() < reader.length) {
        val id = decoder.readId(reader)
        val definition = array[id]
        decoder.readLoop(definition, reader)
        decoder.changeValues(definition)
    }
    return array
}

inline fun <reified T : Definition> load(cache: Cache, decoder: DefinitionDecoder<T>): Array<T> {
    val size = decoder.size(cache)
    val array = Array(size) { decoder.create(it) }
    for (archiveId in cache.getArchives(decoder.index)) {
        val files = cache.getArchiveData(decoder.index, archiveId) ?: continue
        for ((fileId, file) in files) {
            if (file == null) {
                continue
            }
            val id = decoder.id(archiveId, fileId)
            val definition = array[id]
            decoder.readLoop(definition, BufferReader(file))
            decoder.changeValues(definition)
        }
    }
    return array
}