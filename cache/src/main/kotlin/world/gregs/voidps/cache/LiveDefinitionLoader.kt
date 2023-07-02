package world.gregs.voidps.cache

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader
import java.io.File

class LiveDefinitionLoader(
    private val directory: File
) : DefinitionLoader {
    private val logger = InlineLogger()

    override fun <T : Definition> load(decoder: DefinitionDecoder<T>): Array<T> {
        val start = System.currentTimeMillis()
        val file = directory.resolve(decoder.fileName())
        if (!file.exists()) {
            return decoder.create(0)
        }
        val reader = BufferReader(file.readBytes())
        val size = reader.readInt() + 1
        val array = decoder.create(size)
        while (reader.position() < reader.length) {
            decoder.load(array, reader)
        }
        logger.info { "${decoder::class.simpleName} loaded $size in ${System.currentTimeMillis() - start}ms" }
        return array
    }
}