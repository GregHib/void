import world.gregs.voidps.bot.isBot
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Moving
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.Viewport
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.chunk.DynamicChunks
import world.gregs.voidps.engine.map.chunk.ReloadChunk
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.RegionLogin
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.encode.dynamicMapRegion
import world.gregs.voidps.network.encode.mapRegion

/**
 * Keeps track of when players enter and move between regions
 * Loads maps when they are accessed
 */

val xteas: Xteas by inject()
val players: Players by inject()
val dynamicChunks: DynamicChunks by inject()

val playerRegions = IntArray(MAX_PLAYERS - 1)

private val blankXtea = IntArray(4)

on<RegionLogin>({ it.networked }) { player: Player ->
    val viewport = player.viewport ?: return@on
    players.forEach { other ->
        viewport.seen(other)
    }
    updateRegion(player, true, crossedDynamicBoarder(player))
}

/*
    Player regions
 */
on<Registered> { player: Player ->
    player.viewport?.seen(player)
    playerRegions[player.index - 1] = player.tile.regionPlane.id
}

on<Unregistered> { player: Player ->
    playerRegions[player.index - 1] = 0
}

/*
    Region updating
 */
on<Moving>({ from.regionPlane != to.regionPlane }) { player: Player ->
    playerRegions[player.index - 1] = to.regionPlane.id
}

on<Moving>({ it.networked && needsRegionChange(it) }, Priority.HIGH) { player: Player ->
    updateRegion(player, false, crossedDynamicBoarder(player))
}

on<World, ReloadChunk> {
    players.forEach { player ->
        if (player.networked && inViewOfChunk(player, chunk)) {
            updateRegion(player, initial = false, force = true)
        }
    }
}

fun needsRegionChange(player: Player) = !inViewOfChunk(player, player.viewport!!.lastLoadChunk) || crossedDynamicBoarder(player)

fun inViewOfChunk(player: Player, chunk: Chunk): Boolean {
    val viewport = player.viewport!!
    val radius: Int = calculateChunkUpdateRadius(viewport) - 1
    return Distance.within(player.tile.chunk.x, player.tile.chunk.y, chunk.x, chunk.y, radius)
}

fun crossedDynamicBoarder(player: Player) = player.viewport!!.dynamic != inDynamicView(player)

fun inDynamicView(player: Player) = player.tile.chunk.toCuboid(radius = calculateVisibleRadius(player.viewport!!)).toChunks().any(dynamicChunks::isDynamic)

fun calculateVisibleRadius(viewport: Viewport) = calculateChunkUpdateRadius(viewport) / 2 + 1

fun calculateChunkUpdateRadius(viewport: Viewport) = viewport.chunkRadius - 1

fun updateRegion(player: Player, initial: Boolean, force: Boolean) {
    val dynamic = inDynamicView(player)
    val viewport = player.viewport!!
    val wasDynamic = viewport.dynamic
    if (dynamic) {
        updateDynamic(player, initial, force)
    } else {
        update(player, initial, force)
    }
    if ((dynamic || wasDynamic) && !initial) {
        viewport.npcs.clear()
    }
    if (!player.isBot) {
        viewport.loaded = false
    }
    viewport.lastLoadChunk = player.tile.chunk
}

fun update(player: Player, initial: Boolean, force: Boolean) {
    val viewport = player.viewport ?: return
    val xteaList = mutableListOf<IntArray>()

    val chunk = player.tile.chunk
    val chunkX = chunk.x
    val chunkY = chunk.y

    val radius = viewport.chunkRadius
    for (regionX in (chunk.x - radius) / 8..(chunk.x + radius) / 8) {
        for (regionY in (chunk.y - radius) / 8..(chunk.y + radius) / 8) {
            val xtea = xteas[Region.getId(regionX, regionY)] ?: blankXtea
            xteaList.add(xtea)
        }
    }

    viewport.dynamic = false
    player.client?.mapRegion(
        chunkX = chunkX,
        chunkY = chunkY,
        forceRefresh = force,
        mapSize = Viewport.VIEWPORT_SIZES.indexOf(viewport.tileSize),
        xteas = xteaList.toTypedArray(),
        clientIndex = if (initial) player.index - 1 else null,
        playerRegions = if (initial) playerRegions else null,
        clientTile = if (initial) player.tile.id else null
    )
}

fun updateDynamic(player: Player, initial: Boolean, force: Boolean) {
    val viewport = player.viewport ?: return

    val xteaList = mutableListOf<IntArray>()
    val chunks = mutableListOf<Int?>()

    val view = player.tile.chunk.minus(viewport.chunkRadius, viewport.chunkRadius)
    val chunkSize = viewport.chunkArea
    var append = 0
    for (origin in view.toCuboid(chunkSize, chunkSize).copy(minPlane = 0, maxPlane = 3).toChunks()) {
        val mapChunk = dynamicChunks.getDynamicChunk(origin)
        if (mapChunk == null) {
            chunks.add(null)
            continue
        }
        val (target, region) = mapChunk
        chunks.add(target)
        val xtea = xteas[region] ?: blankXtea
        if (!xteaList.contains(xtea)) {
            xteaList.add(xtea)
        } else {
            append++
        }
    }
    repeat(append) {
        xteaList.add(blankXtea)
    }
    viewport.dynamic = true
    player.client?.dynamicMapRegion(
        chunkX = player.tile.chunk.x,
        chunkY = player.tile.chunk.y,
        forceRefresh = force,
        mapSize = Viewport.VIEWPORT_SIZES.indexOf(viewport.tileSize),
        chunks = chunks,
        xteas = xteaList.toTypedArray(),
        clientIndex = if (initial) player.index - 1 else null,
        playerRegions = if (initial) playerRegions else null,
        clientTile = if (initial) player.tile.id else null
    )
}