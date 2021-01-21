package world.gregs.void.cache.config.decoder

import world.gregs.void.buffer.read.Reader
import world.gregs.void.cache.Cache
import world.gregs.void.cache.Configs.IDENTITY_KIT
import world.gregs.void.cache.config.ConfigDecoder
import world.gregs.void.cache.config.data.IdentityKitDefinition

/**
 * @author GregHib <greg@gregs.world>
 * @since April 08, 2020
 */
class IdentityKitDecoder(cache: Cache) : ConfigDecoder<IdentityKitDefinition>(cache, IDENTITY_KIT) {

    override fun create() = IdentityKitDefinition()

    override fun IdentityKitDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> bodyPartId = buffer.readUnsignedByte()
            2 -> {
                val length = buffer.readUnsignedByte()
                modelIds = IntArray(length)
                repeat(length) { count ->
                    modelIds!![count] = buffer.readUnsignedShort()
                }
            }
            40 -> readColours(buffer)
            41 -> readTextures(buffer)
            in 60..69 -> headModels[opcode - 60] = buffer.readUnsignedShort()
        }
    }
}