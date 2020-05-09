import rs.dusk.engine.client.send
import rs.dusk.engine.client.verify.verify
import rs.dusk.engine.entity.event.Deregistered
import rs.dusk.engine.entity.event.Registered
import rs.dusk.engine.entity.list.MAX_PLAYERS
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.map.location.Xtea
import rs.dusk.engine.map.location.Xteas
import rs.dusk.engine.model.Region
import rs.dusk.engine.model.entity.Move
import rs.dusk.network.rs.codec.game.encode.message.MapRegionMessage
import rs.dusk.network.rs.codec.login.decode.message.GameLoginMessage
import rs.dusk.utility.inject

val xteas: Xteas by inject()
val players: Players by inject()

val regions = IntArray(MAX_PLAYERS - 1)

Registered where { entity is Player } then {
    val player = entity as Player
    regions[player.index - 1] = player.tile.regionPlane.id
}

Deregistered where { entity is Player } then {
    val player = entity as Player
    regions[player.index - 1] = 0
}

GameLoginMessage verify { player ->
    calculateRegions(player, true)
}

Move where { entity is Player && from.chunk != to.chunk } then {
    calculateRegions(entity as Player, false)
}

fun Int.nearby(size: Int): IntRange {
    return (this - size) / 8..(this + size) / 8
}

// FIXME prevent or buffer movement until region load is complete or calculate irrespective of movement?
fun calculateRegions(player: Player, initial: Boolean) {
    val regions = player.viewport.regions
    val size = player.viewport.size shr 4
    val chunk = player.tile.chunk
    val before = regions.hashCode()

    regions.clear()
    for (regionX in chunk.x.nearby(size)) {
        for (regionY in chunk.y.nearby(size)) {
            regions.add(Region.getId(regionX, regionY))
        }
    }

    val after = regions.hashCode()
    if (before != after) {
        update(player, initial)
    }
}

fun update(player: Player, initial: Boolean) {
    val list = mutableListOf<Xtea>()

    val chunkX = player.tile.chunk.x
    val chunkY = player.tile.chunk.y

    player.viewport.regions.forEach { regionId ->
        val xtea = xteas[regionId] ?: IntArray(4)
        list.add(xtea)
    }

    if (initial) {
        regions.forEachIndexed { index, _ ->
            val p = players.getAtIndex(index) ?: return@forEachIndexed
            player.viewport.players.lastSeen[p] = p.tile
        }
    }

    player.send(
        MapRegionMessage(
            chunkX = chunkX,
            chunkY = chunkY,
            forceReload = false,
            mapSize = 0,
            xteas = list.toTypedArray(),
            clientIndex = if (initial) player.index - 1 else null,
            playerRegions = if (initial) regions else null,
            clientTile = if (initial) player.tile.id else null
        )
    )
}