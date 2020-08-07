package rs.dusk.cache.config.decoder

import rs.dusk.cache.Cache
import rs.dusk.cache.Configs.IDENTITY_KIT
import rs.dusk.cache.config.ConfigDecoder
import rs.dusk.cache.config.data.IdentityKitDefinition
import rs.dusk.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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