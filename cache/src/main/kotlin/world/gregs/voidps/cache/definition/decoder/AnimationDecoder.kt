package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.ANIMATIONS
import world.gregs.voidps.cache.definition.data.AnimationDefinition

class AnimationDecoder : DefinitionDecoder<AnimationDefinition>(ANIMATIONS) {

    override fun create(size: Int) = Array(size) { AnimationDefinition(it) }

    override fun getFile(id: Int) = id and 0x7f

    override fun getArchive(id: Int) = id ushr 7

    override fun size(cache: Cache): Int = cache.lastArchiveId(index) * 128 + (cache.fileCount(index, cache.lastArchiveId(index)))

    override fun AnimationDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> buffer.skip(buffer.readShort() * 6)
            2, 6, 7, 19 -> buffer.skip(2)
            3 -> buffer.skip(buffer.readUnsignedByte())
            5 -> priority = buffer.readUnsignedByte()
            8, 9, 10, 11 -> buffer.skip(1)
            12 -> buffer.skip(buffer.readUnsignedByte() * 4)
            13 -> {
                val length = buffer.readShort()
                for (count in 0 until length) {
                    val size = buffer.readUnsignedByte()
                    if (size > 0) {
                        buffer.readUnsignedMedium()
                        buffer.skip((size - 1) * 2)
                    }
                }
            }
            14, 15, 18 -> return
            20 -> buffer.skip(5)
        }
    }
}
