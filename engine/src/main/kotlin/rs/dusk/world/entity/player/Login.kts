import rs.dusk.engine.client.send
import rs.dusk.engine.client.verify.verify
import rs.dusk.engine.entity.event.Deregistered
import rs.dusk.engine.entity.event.Registered
import rs.dusk.engine.entity.list.MAX_PLAYERS
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.map.location.Xtea
import rs.dusk.engine.map.location.Xteas
import rs.dusk.network.rs.codec.game.encode.message.MapRegionMessage
import rs.dusk.network.rs.codec.login.decode.message.GameLoginMessage
import rs.dusk.utility.inject

val xteas: Xteas by inject()

fun forNearbyRegions(chunkX: Int, chunkY: Int, mapHash: Int, action: (Int) -> Unit) {
    for (regionX in (chunkX - mapHash) / 8..(chunkX + mapHash) / 8) {
        for (regionY in (chunkY - mapHash) / 8..(chunkY + mapHash) / 8) {
            action(regionY + (regionX shl 8))
        }
    }
}

val regions = IntArray(MAX_PLAYERS - 1)

Registered where { entity is Player } then {
    val player = entity as Player
    regions[player.index - 1] = player.tile.regionPlane.id
}

Deregistered where { entity is Player } then {
    val player = entity as Player
    regions[player.index - 1] = 0
}

// TODO on region or plane change update positions

GameLoginMessage verify { player ->
    val list = mutableListOf<Xtea>()

    val chunkX = player.tile.chunk.x
    val chunkY = player.tile.chunk.y

    forNearbyRegions(chunkX, chunkY, 6) { regionId ->
        val xtea = xteas[regionId] ?: IntArray(4)
        list.add(xtea)
    }

    player.send(
        MapRegionMessage(
            chunkX = chunkX,
            chunkY = chunkY,
            forceReload = false,
            mapSize = 0,
            xteas = list.toTypedArray(),
            clientIndex = player.index - 1,
            playerRegions = regions,
            clientTile = player.tile.id
        )
    )
}