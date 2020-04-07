package org.redrune.cache.config.decoder

import org.redrune.cache.Configs.IDENTITY_KIT
import org.redrune.cache.config.ConfigDecoder
import org.redrune.cache.config.data.IdentityKitDefinition
import org.redrune.storage.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class IdentityKitDecoder : ConfigDecoder<IdentityKitDefinition>(IDENTITY_KIT) {

    override fun create() = IdentityKitDefinition()

    override fun IdentityKitDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> buffer.readUnsignedByte()
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