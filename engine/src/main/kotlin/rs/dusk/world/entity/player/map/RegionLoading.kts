import rs.dusk.engine.client.send
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.Unregistered
import rs.dusk.engine.model.entity.character.Moved
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.player.PlayerRegistered
import rs.dusk.engine.model.entity.character.player.Players
import rs.dusk.engine.model.entity.list.MAX_PLAYERS
import rs.dusk.engine.model.world.DynamicMaps
import rs.dusk.engine.model.world.Region
import rs.dusk.engine.model.world.ReloadRegion
import rs.dusk.engine.model.world.map.MapReader
import rs.dusk.engine.model.world.map.obj.Xteas
import rs.dusk.network.rs.codec.game.encode.message.DynamicMapRegionMessage
import rs.dusk.network.rs.codec.game.encode.message.MapRegionMessage
import rs.dusk.utility.func.nearby
import rs.dusk.utility.inject
import rs.dusk.world.entity.player.map.RegionInitialLoad
import kotlin.math.abs

/**
 * Keeps track of when players enter and move between regions
 * Loads maps when they are accessed
 * Emits [RegionMapUpdate] events when a players region has changed
 */

val maps: MapReader by inject()
val xteas: Xteas by inject()
val players: Players by inject()
val dynamicMaps: DynamicMaps by inject()

val playerRegions = IntArray(MAX_PLAYERS - 1)

private val blankXtea = IntArray(4)

RegionInitialLoad then {
    players.forEach { other ->
        player.viewport.players.lastSeen[other] = other.tile
    }
    calculateRegions(player, true)
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

Moved where { entity is Player && from.regionPlane != to.regionPlane } then {
    val player = entity as Player
    playerRegions[player.index - 1] = to.regionPlane.id
}

Moved where { entity is Player && needsRegionChange(entity) } then {
    val player = entity as Player
    calculateRegions(player, false)
}

ReloadRegion then {
    val regionId = region.id
    players.forEach {
        if(it.viewport.regions.contains(regionId)) {
            updateRegion(it, initial = false, force = true)
        }
    }
}

fun needsRegionChange(player: Player): Boolean {
    val size: Int = (player.viewport.size shr 3) / 2 - 1
    val delta = player.viewport.lastLoadChunk.delta(player.tile.chunk)
    return abs(delta.x) >= size || abs(delta.y) >= size
}

fun calculateRegions(player: Player, initial: Boolean) {
    val regions = player.viewport.regions
    val size = player.viewport.size shr 4
    val chunk = player.tile.chunk
    regions.clear()
    for(regionX in (chunk.x - size) / 8..(chunk.x + size) / 8) {
        for(regionY in (chunk.y - size) / 8..(chunk.y + size) / 8) {
            regions.add(Region.getId(regionX, regionY))
        }
    }
    updateRegion(player, initial, false)
}

fun updateRegion(player: Player, initial: Boolean, force: Boolean) {
    val dynamic = player.viewport.regions.any { dynamicMaps.regions.contains(it) }
    if (dynamic) {
        updateDynamic(player, initial, force)
    } else {
        update(player, initial, force)
    }
    if(force) {
        player.viewport.npcs.refresh()
        player.viewport.players.refresh(player)
    }
    player.viewport.lastLoadChunk = player.tile.chunk
}

fun update(player: Player, initial: Boolean, force: Boolean) {
    val xteaList = mutableListOf<IntArray>()

    val chunk = player.tile.chunk
    val chunkX = chunk.x
    val chunkY = chunk.y

    player.viewport.regions.forEach { regionId ->
        val xtea = xteas[regionId] ?: blankXtea
        xteaList.add(xtea)
    }

    player.viewport.loaded = false
    player.send(
        MapRegionMessage(
            chunkX = chunkX,
            chunkY = chunkY,
            forceReload = force,
            mapSize = 0,
            xteas = xteaList.toTypedArray(),
            clientIndex = if (initial) player.index - 1 else null,
            playerRegions = if (initial) playerRegions else null,
            clientTile = if (initial) player.tile.id else null
        )
    )
    player.viewport.lastLoadChunk = player.tile.chunk
}

fun updateDynamic(player: Player, initial: Boolean, force: Boolean) {
    val xteaList = mutableListOf<IntArray>()

    val chunkX = player.tile.chunk.x
    val chunkY = player.tile.chunk.y

    val chunks = mutableListOf<Int?>()
    val mapTileSize = player.viewport.size shr 4

    for (z in 0 until 4) {
        for (x in player.tile.chunk.x.nearby(mapTileSize)) {
            for (y in player.tile.chunk.y.nearby(mapTileSize)) {
                val mapChunk = dynamicMaps.chunks[DynamicMaps.toChunkPosition(x, y, z)]
                if (mapChunk != null) {
                    chunks.add(mapChunk)
                    xteaList.add(xteas[Region.getId(x / 8, y / 8)] ?: blankXtea)
                } else {
                    chunks.add(null)
                }
            }
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
        DynamicMapRegionMessage(
            chunkX = chunkX,
            chunkY = chunkY,
            forceReload = force,
            mapSize = 0,
            chunks = chunks,
            xteas = xteaList.toTypedArray(),
            clientIndex = if (initial) player.index - 1 else null,
            playerRegions = if (initial) playerRegions else null,
            clientTile = if (initial) player.tile.id else null
        )
    )
}