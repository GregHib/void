import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.Viewport
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.chunk.DynamicChunks
import world.gregs.voidps.engine.map.chunk.ReloadChunk
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.RegionLogin
import world.gregs.voidps.engine.map.region.RegionReader
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.network.encode.dynamicMapRegion
import world.gregs.voidps.network.encode.mapRegion
import world.gregs.voidps.engine.utility.inject

/**
 * Keeps track of when players enter and move between regions
 * Loads maps when they are accessed
 */

val maps: RegionReader by inject()
val xteas: Xteas by inject()
val players: Players by inject()
val dynamicChunks: DynamicChunks by inject()

val playerRegions = IntArray(MAX_PLAYERS - 1)

private val blankXtea = IntArray(4)

on<RegionLogin>({ it.client != null }) { player: Player ->
    players.forEach { other ->
        player.viewport.players.lastSeen[other] = other.tile
    }
    updateRegion(player, true, crossedDynamicBoarder(player))
}

/*
    Collision map loading
 */
on<Registered> { player: Player ->
    load(player)
}

on<Moved> { character: Character ->
    load(character)
}

fun load(character: Character) {
    character.tile.region.add(-1, -1).toRectangle(3, 3).toRegions().forEach {
        maps.load(it)
    }
}

/*
    Player regions
 */
on<Registered> { player: Player ->
    playerRegions[player.index - 1] = player.tile.regionPlane.id
}

on<Unregistered> { player: Player ->
    playerRegions[player.index - 1] = 0
}

/*
    Region updating
 */
on<Moved>({ from.regionPlane != to.regionPlane }) { player: Player ->
    playerRegions[player.index - 1] = to.regionPlane.id
}

on<Moved>({ it.client != null && needsRegionChange(it) }) { player: Player ->
    updateRegion(player, false, crossedDynamicBoarder(player))
}

on<World, ReloadChunk> {
    players.forEach { player ->
        if (player.client != null && inViewOfChunk(player, chunk)) {
            updateRegion(player, initial = false, force = true)
        }
    }
}

fun needsRegionChange(player: Player) = !inViewOfChunk(player, player.viewport.lastLoadChunk) || crossedDynamicBoarder(player)

fun inViewOfChunk(player: Player, chunk: Chunk): Boolean {
    val viewport = player.viewport
    val radius: Int = calculateChunkUpdateRadius(viewport) - 1
    return Distance.within(player.tile.chunk.x, player.tile.chunk.y, chunk.x, chunk.y, radius)
}

fun crossedDynamicBoarder(player: Player) = player.viewport.dynamic != inDynamicView(player)

fun inDynamicView(player: Player) = player.tile.chunk.toCuboid(radius = calculateVisibleRadius(player.viewport)).toChunks().any { dynamicChunks.chunks.containsKey(it.id) }

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
    player.client?.mapRegion(
        chunkX = chunkX,
        chunkY = chunkY,
        forceRefresh = force,
        mapSize = Viewport.VIEWPORT_SIZES.indexOf(player.viewport.tileSize),
        xteas = xteaList.toTypedArray(),
        clientIndex = if (initial) player.index - 1 else null,
        playerRegions = if (initial) playerRegions else null,
        clientTile = if (initial) player.tile.id else null
    )
}

fun updateDynamic(player: Player, initial: Boolean, force: Boolean) {
    val xteaList = mutableListOf<IntArray>()

    val chunkX = player.tile.chunk.x
    val chunkY = player.tile.chunk.y

    val chunks = mutableListOf<Int?>()
    val mapTileSize = calculateChunkRadius(player.viewport)

    for (chunk in player.tile.chunk.toCuboid(mapTileSize).copy(minPlane = 0, maxPlane = 3)) {
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
    player.client?.dynamicMapRegion(
        chunkX = chunkX,
        chunkY = chunkY,
        forceRefresh = force,
        mapSize = Viewport.VIEWPORT_SIZES.indexOf(player.viewport.tileSize),
        chunks = chunks,
        xteas = xteaList.toTypedArray(),
        clientIndex = if (initial) player.index - 1 else null,
        playerRegions = if (initial) playerRegions else null,
        clientTile = if (initial) player.tile.id else null
    )
}