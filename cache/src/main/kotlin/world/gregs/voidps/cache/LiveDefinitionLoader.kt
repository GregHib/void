package world.gregs.voidps.cache

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader
import java.io.File

class LiveDefinitionLoader(
    directory: File
) : DefinitionLoader {
    private val logger = InlineLogger()
    private val directory: File = directory.resolve("live/")

    override fun <T : Definition> load(decoder: DefinitionDecoder<T>): Array<T> {
        val start = System.currentTimeMillis()
        val file = directory.resolve(decoder.fileName())
        if (!file.exists()) {
            return decoder.create(0)
        }
        val reader = BufferReader(file.readBytes())
        val size = reader.readInt() + 1
        decoder.last = size - 1
        val array = decoder.create(size)
        while (reader.position() < reader.length) {
            decoder.load(array, reader)
        }
        logger.info { "$size ${decoder::class.simpleName} definitions loaded in ${System.currentTimeMillis() - start}ms" }
        return array
    }
}