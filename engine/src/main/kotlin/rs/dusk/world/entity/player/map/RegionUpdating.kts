import rs.dusk.engine.client.session.send
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.Unregistered
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerRegistered
import rs.dusk.engine.model.entity.index.player.Players
import rs.dusk.engine.model.entity.list.MAX_PLAYERS
import rs.dusk.engine.model.world.map.location.Xtea
import rs.dusk.engine.model.world.map.location.Xteas
import rs.dusk.network.rs.codec.game.encode.message.MapRegionMessage
import rs.dusk.utility.inject
import rs.dusk.world.entity.player.map.RegionMapUpdate

/**
 * Tracks players regions and sends map load messages when a player nears the border of their current regions
 */

val xteas: Xteas by inject()
val players: Players by inject()

val regions = IntArray(MAX_PLAYERS - 1)

PlayerRegistered then {
    regions[player.index - 1] = player.tile.regionPlane.id
}

Unregistered where { entity is Player } then {
    val player = entity as Player
    regions[player.index - 1] = 0
}

RegionMapUpdate then {
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

    println("Load region $chunkX $chunkY ${player.tile.region.id}")
    player.viewport.loaded = false
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