package rs.dusk.engine.model.world.map.obj

import org.koin.dsl.module
import rs.dusk.core.io.read.BufferReader
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.obj.GameObject
import rs.dusk.engine.model.entity.obj.Objects
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.BRIDGE_TILE
import rs.dusk.engine.model.world.map.TileSettings
import rs.dusk.engine.model.world.map.isTile

val objectMapModule = module {
    single { GameObjectMapReader(get(), get()) }
}

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
class GameObjectMapReader(val objects: Objects, val bus: EventBus) {

    fun read(region: Tile, data: ByteArray, settings: TileSettings) {
        val reader = BufferReader(data)
        var objectId = -1
        while (true) {
            val skip = reader.readLargeSmart()
            if (skip == 0) {
                break
            }
            objectId += skip
            var tile = 0
            while (true) {
                val loc = reader.readSmart()
                if (loc == 0) {
                    break
                }
                tile += loc - 1

                // Data
                val localX = tile shr 6 and 0x3f
                val localY = tile and 0x3f
                var plane = tile shr 12
                val obj = reader.readUnsignedByte()
                val type = obj shr 2
                val rotation = obj and 0x3

                // Validate region
                if (localX < 0 || localX > 64 || localY < 0 || localY >= 64) {
                    continue
                }

                // Decrease bridges
                if (settings.isTile(1, localX, localY, BRIDGE_TILE)) {
                    plane--
                }

                // Validate plane
                if (plane !in 0 until 4) {
                    continue
                }

                // Valid location
                val gameObject = GameObject(objectId, Tile(region.x + localX, region.y + localY, plane), type, rotation)
                objects.add(gameObject)
                bus.emit(Registered(gameObject))
            }
        }
    }
}