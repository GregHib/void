package world.gregs.voidps.cache.definition.encoder

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.DefinitionEncoder
import world.gregs.voidps.cache.definition.data.GraphicDefinition

class GraphicEncoder : DefinitionEncoder<GraphicDefinition> {

    override fun Writer.encode(definition: GraphicDefinition, members: GraphicDefinition) {
        if (definition.id == -1) {
            return
        }

        if (definition.modelId != 0) {
            writeByte(1)
            writeShort(definition.modelId)
        }

        if (definition.animationId != -1) {
            writeByte(2)
            writeShort(definition.animationId)
        }

        if (definition.sizeXY != 128) {
            writeByte(4)
            writeShort(definition.sizeXY)
        }

        if (definition.sizeZ != 128) {
            writeByte(5)
            writeShort(definition.sizeZ)
        }

        if (definition.rotation != 0) {
            writeByte(6)
            writeShort(definition.rotation)
        }

        if (definition.ambience != 0) {
            writeByte(7)
            writeByte(definition.ambience)
        }

        if (definition.contrast != 0) {
            writeByte(8)
            writeByte(definition.contrast)
        }

        when (definition.aByte2381.toInt()) {
            1 -> writeByte(11)
            2 -> {
                writeByte(14)
                writeByte(definition.anInt2385 / 256)
            }
            3 -> when {
                definition.anInt2385 == 8224 -> writeByte(9)
                definition.anInt2385 in 0..0xffff -> {
                    writeByte(15)
                    writeShort(definition.anInt2385)
                }
                else -> {
                    writeByte(16)
                    writeInt(definition.anInt2385)
                }
            }
            4 -> writeByte(12)
            5 -> writeByte(13)
        }

        if (definition.aBoolean2402) {
            writeByte(10)
        }

        definition.writeColoursTextures(this)
        writeByte(0)
    }
}
