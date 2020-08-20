import rs.dusk.engine.client.send
import rs.dusk.engine.entity.Registered
import rs.dusk.engine.entity.Unregistered
import rs.dusk.engine.entity.character.move.NPCMoved
import rs.dusk.engine.entity.character.move.PlayerMoved
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerRegistered
import rs.dusk.engine.entity.character.player.Players
import rs.dusk.engine.entity.character.player.Viewport
import rs.dusk.engine.entity.list.MAX_PLAYERS
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.map.area.area
import rs.dusk.engine.map.chunk.Chunk
import rs.dusk.engine.map.chunk.DynamicChunks
import rs.dusk.engine.map.chunk.ReloadChunk
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.RegionLogin
import rs.dusk.engine.map.region.RegionReader
import rs.dusk.engine.map.region.obj.Xteas
import rs.dusk.network.rs.codec.game.encode.message.DynamicMapRegionMessage
import rs.dusk.network.rs.codec.game.encode.message.MapRegionMessage
import rs.dusk.utility.inject
import kotlin.math.abs

/**
 * Keeps track of when players enter and move between regions
 * Loads maps when they are accessed
 * Emits [RegionMapUpdate] events when a players region has changed
 */

val maps: RegionReader by inject()
val xteas: Xteas by inject()
val players: Players by inject()
val dynamicChunks: DynamicChunks by inject()

val playerRegions = IntArray(MAX_PLAYERS - 1)

private val blankXtea = IntArray(4)

RegionLogin then {
    players.forEach { other ->
        player.viewport.players.lastSeen[other] = other.tile
    }
    updateRegion(player, true, crossedDynamicBoarder(player))
}

/*
    Collision map loading
 */
Registered where { entity is Player } then {
    maps.load(entity.tile.region)
}

PlayerMoved then {
    maps.load(player.tile.region)
}

NPCMoved then {
    maps.load(npc.tile.region)
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

PlayerMoved where { from.regionPlane != to.regionPlane } then {
    playerRegions[player.index - 1] = to.regionPlane.id
}

PlayerMoved where { needsRegionChange(player) } then {
    updateRegion(player, false, crossedDynamicBoarder(player))
}

ReloadChunk then {
    players.forEach { player ->
        if (inViewOfChunk(player, chunk)) {
            updateRegion(player, initial = false, force = true)
        }
    }
}

fun needsRegionChange(player: Player) = !inViewOfChunk(player, player.viewport.lastLoadChunk) || crossedDynamicBoarder(player)

fun inViewOfChunk(player: Player, chunk: Chunk): Boolean {
    val viewport = player.viewport
    val radius: Int = calculateChunkUpdateRadius(viewport)
    val delta = player.tile.chunk.delta(chunk)
    return abs(delta.x) < radius && abs(delta.y) < radius
}

fun crossedDynamicBoarder(player: Player) = player.viewport.dynamic != inDynamicView(player)

fun inDynamicView(player: Player) = player.tile.chunk.area(calculateVisibleRadius(player.viewport)).any { dynamicChunks.chunks.containsKey(it.id) }

fun calculateVisibleRadius(viewport: Viewport) = calculateChunkUpdateRadius(viewport) / 2 + 1

fun calculateChunkUpdateRadius(viewport: Viewport) = calculateChunkRadius(viewport) - 1

fun calculateChunkRadius(viewport: Viewport) = viewport.tileSize shr 4

fun updateRegion(player: Player, initial: Boolean, force: Boolean) {
    val dynamic = inDynamicView(player)
    val wasDynamic = player.viewport.dynamic
    if (dynamic) {
        updateDynamic(player, initial, force)
    } else {
        update(player, initial, force)
    }
    if ((dynamic || wasDynamic) && !initial) {
        player.viewport.npcs.refresh()
    }
    player.viewport.loaded = false
    player.viewport.lastLoadChunk = player.tile.chunk
}

fun update(player: Player, initial: Boolean, force: Boolean) {
    val xteaList = mutableListOf<IntArray>()

    val chunk = player.tile.chunk
    val chunkX = chunk.x
    val chunkY = chunk.y

    val viewport = player.viewport
    val radius = calculateChunkRadius(viewport)
    for (regionX in (chunk.x - radius) / 8..(chunk.x + radius) / 8) {
        for (regionY in (chunk.y - radius) / 8..(chunk.y + radius) / 8) {
            val xtea = xteas[Region.getId(regionX, regionY)] ?: blankXtea
            xteaList.add(xtea)
        }
    }

    player.viewport.dynamic = false
    player.send(
        MapRegionMessage(
            chunkX = chunkX,
            chunkY = chunkY,
            forceReload = force,
            mapSize = Viewport.VIEWPORT_SIZES.indexOf(player.viewport.tileSize),
            xteas = xteaList.toTypedArray(),
            clientIndex = if (initial) player.index - 1 else null,
            playerRegions = if (initial) playerRegions else null,
            clientTile = if (initial) player.tile.id else null
        )
    )
}

fun updateDynamic(player: Player, initial: Boolean, force: Boolean) {
    val xteaList = mutableListOf<IntArray>()

    val chunkX = player.tile.chunk.x
    val chunkY = player.tile.chunk.y

    val chunks = mutableListOf<Int?>()
    val mapTileSize = calculateChunkRadius(player.viewport)

    for(chunk in player.tile.chunk.copy(plane = 0).area(mapTileSize, 4)) {
        val mapChunk = dynamicChunks.chunks[chunk.id]
        if (mapChunk != null) {
            chunks.add(mapChunk)
            val xtea = xteas[chunk.region.id] ?: blankXtea
            if (!xteaList.contains(xtea)) {
                xteaList.add(xtea)
            }
        } else {
            chunks.add(null)
        }
    }

    player.viewport.dynamic = true
    player.send(
        DynamicMapRegionMessage(
            chunkX = chunkX,
            chunkY = chunkY,
            forceReload = force,
            mapSize = Viewport.VIEWPORT_SIZES.indexOf(player.viewport.tileSize),
            chunks = chunks,
            xteas = xteaList.toTypedArray(),
            clientIndex = if (initial) player.index - 1 else null,
            playerRegions = if (initial) playerRegions else null,
            clientTile = if (initial) player.tile.id else null
        )
    )
}