package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.GRAPHICS
import world.gregs.voidps.cache.definition.data.GraphicDefinition

class GraphicDecoder : DefinitionDecoder<GraphicDefinition>(GRAPHICS) {

    override fun create(size: Int) = Array(size) { GraphicDefinition(it) }

    override fun getFile(id: Int) = id and 0xff

    override fun getArchive(id: Int) = id ushr 8

    override fun GraphicDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> modelId = buffer.readShort()
            2 -> animationId = buffer.readShort()
            4 -> sizeXY = buffer.readShort()
            5 -> sizeZ = buffer.readShort()
            6 -> rotation = buffer.readShort()
            7 -> ambience = buffer.readUnsignedByte()
            8 -> contrast = buffer.readUnsignedByte()
            9 -> {
                aByte2381 = 3
                anInt2385 = 8224
            }
            10 -> aBoolean2402 = true
            11 -> aByte2381 = 1.toByte()
            12 -> aByte2381 = 4.toByte()
            13 -> aByte2381 = 5.toByte()
            14 -> {
                aByte2381 = 2
                anInt2385 = 256 * buffer.readUnsignedByte()
            }
            15 -> {
                aByte2381 = 3
                anInt2385 = buffer.readShort()
            }
            16 -> {
                aByte2381 = 3
                anInt2385 = buffer.readInt()
            }
            40 -> readColours(buffer)
            41 -> readTextures(buffer)
        }
    }
}
