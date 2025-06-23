package world.gregs.voidps.cache.definition.encoder

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.DefinitionEncoder
import world.gregs.voidps.cache.definition.data.MapDefinition

class MapTileEncoder : DefinitionEncoder<MapDefinition> {

    override fun Writer.encode(definition: MapDefinition) {
        if (definition.id == -1) {
            return
        }

        for (level in 0 until 4) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    val tile = definition.getTile(localX, localY, level)
                    if (tile.underlayId != 0) {
                        writeByte(tile.underlayId + 81)
                    }
                    if (tile.settings != 0) {
                        writeByte(tile.settings + 49)
                    }
                    if (tile.attrOpcode != 0) {
                        writeByte(tile.attrOpcode)
                        writeByte(tile.overlayId)
                    }
                    if (tile.height != 0) {
                        writeByte(1)
                        writeByte(tile.height)
                    }
                    writeByte(0)
                }
            }
        }
    }
}
