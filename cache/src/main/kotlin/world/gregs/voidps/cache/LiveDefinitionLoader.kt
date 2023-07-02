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
        val reader = BufferReader(directory.resolve(fileName(decoder.index)).readBytes())
        val size = reader.readInt()
        val array = decoder.create(size)
        while (reader.position() < reader.length) {
            val id = decoder.readId(reader)
            val definition = array[id]
            decoder.readLoop(definition, reader)
            decoder.changeDefValues(definition)
        }
        logger.info { "${this::class.simpleName} loaded $size in ${System.currentTimeMillis() - start}ms" }
        return array
    }

    private fun fileName(index: Int) = "index$index.dat"
}