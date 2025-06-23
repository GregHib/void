package world.gregs.voidps.cache.config.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Config.IDENTITY_KIT
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.config.data.IdentityKitDefinition

class IdentityKitDecoder : ConfigDecoder<IdentityKitDefinition>(IDENTITY_KIT) {

    override fun create(size: Int) = Array(size) { IdentityKitDefinition(it) }

    override fun IdentityKitDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> bodyPartId = buffer.readUnsignedByte()
            2 -> {
                val length = buffer.readUnsignedByte()
                modelIds = IntArray(length) { buffer.readUnsignedShort() }
            }
            40 -> readColours(buffer)
            41 -> readTextures(buffer)
            in 60..69 -> headModels[opcode - 60] = buffer.readUnsignedShort()
        }
    }
}
