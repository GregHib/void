import rs.dusk.engine.client.session.send
import rs.dusk.engine.client.verify.verify
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.Unregistered
import rs.dusk.engine.model.entity.index.Moved
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerRegistered
import rs.dusk.engine.model.entity.index.player.Players
import rs.dusk.engine.model.entity.list.MAX_PLAYERS
import rs.dusk.engine.model.world.Region
import rs.dusk.engine.model.world.map.MapReader
import rs.dusk.engine.model.world.map.location.Xtea
import rs.dusk.engine.model.world.map.location.Xteas
import rs.dusk.network.rs.codec.game.decode.message.RegionLoadedMessage
import rs.dusk.network.rs.codec.game.encode.message.MapRegionMessage
import rs.dusk.network.rs.codec.login.decode.message.GameLoginMessage
import rs.dusk.utility.inject
import kotlin.math.abs

/**
 * Keeps track of when players enter and move between regions
 * Loads maps when they are accessed
 * Emits [RegionMapUpdate] events when a players region has changed
 */

val bus: EventBus by inject()
val maps: MapReader by inject()
val xteas: Xteas by inject()
val players: Players by inject()

val playerRegions = IntArray(MAX_PLAYERS - 1)

private val blankXtea = IntArray(4)

GameLoginMessage verify { player ->
    updateRegion(player, true)
    bus.emit(PlayerRegistered(player))
    bus.emit(Registered(player))
}

RegionLoadedMessage verify { player ->
    player.viewport.loaded = true
}

/*
    Collision map loading
 */
Registered where { entity is Player } then {
    maps.load(entity.tile.region)
}

Moved then {
    maps.load(entity.tile.region)
}

/*
    Player regions
 */

PlayerRegistered then {
    playerRegions[player.index - 1] = player.tile.regionPlane.id
}

Unregistered where { entity is Player } then {
    val player = entity as Player
    playerRegions[player.index - 1] = 0
}
/*
    Region updating
 */

Moved where { entity is Player && needsRegionChange(entity) } then {
    updateRegion(entity as Player, false)
}

fun needsRegionChange(player: Player): Boolean {
    val size: Int = (player.viewport.size shr 3) / 2 - 1
    val delta = player.viewport.lastLoadChunk.delta(player.tile.chunk)
    return abs(delta.x) >= size || abs(delta.y) >= size
}

fun updateRegion(player: Player, initial: Boolean) {
    val list = mutableListOf<Xtea>()

    val chunk = player.tile.chunk
    val chunkX = chunk.x
    val chunkY = chunk.y

    val size = player.viewport.size shr 4
    for(regionX in (chunk.x - size) / 8..(chunk.x + size) / 8) {
        for(regionY in (chunk.y - size) / 8..(chunk.y + size) / 8) {
            val regionId = Region.getId(regionX, regionY)
            val xtea = xteas[regionId] ?: blankXtea
            list.add(xtea)
        }
    }

    if (initial) {
        for (index in playerRegions.indices) {
            val p = players.getAtIndex(index) ?: continue
            player.viewport.players.lastSeen[p] = p.tile
        }
    }

    player.viewport.loaded = false
    player.send(
        MapRegionMessage(
            chunkX = chunkX,
            chunkY = chunkY,
            forceReload = false,
            mapSize = 0,
            xteas = list.toTypedArray(),
            clientIndex = if (initial) player.index - 1 else null,
            playerRegions = if (initial) playerRegions else null,
            clientTile = if (initial) player.tile.id else null
        )
    )
    player.viewport.lastLoadChunk = player.tile.chunk
}