import rs.dusk.engine.client.send
import rs.dusk.engine.client.verify.verify
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

fun hash30Bit(x: Int, y: Int, plane: Int = 0): Int {
    return y + (x shl 14) + (plane shl 28)
}

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
            positions = intArrayOf(player.tile.region.id),
            location = hash30Bit(player.tile.x, player.tile.y, player.tile.plane)
        )
    )
}