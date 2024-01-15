package world.gregs.voidps.world.interact.world.map

import world.gregs.voidps.bot.isBot
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.entity.MAX_PLAYERS
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.region.RegionRetry
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.engine.map.zone.ReloadZone
import world.gregs.voidps.network.encode.dynamicMapRegion
import world.gregs.voidps.network.encode.mapRegion
import world.gregs.voidps.type.Distance
import world.gregs.voidps.type.Zone

/**
 * Keeps track of when players enter and move between regions
 * Loads maps when they are accessed
 */

val players: Players by inject()
val dynamicZones: DynamicZones by inject()

val playerRegions = IntArray(MAX_PLAYERS - 1)

private val blankXtea = IntArray(4)

on<Registered>(priority = Priority.HIGHEST) { player: Player ->
    player.viewport?.seen(player)
    playerRegions[player.index - 1] = player.tile.regionLevel.id
    val viewport = player.viewport ?: return@on
    players.forEach { other ->
        viewport.seen(other)
    }
    updateRegion(player, true, crossedDynamicBoarder(player))
    viewport.players.addSelf(player)
}

on<RegionRetry>({ it.networked }) { player: Player ->
    println("Failed to load region. Retrying...")
    updateRegion(player, initial = false, force = true)
}

/*
    Player regions
 */

on<Unregistered> { player: Player ->
    playerRegions[player.index - 1] = 0
}

/*
    Region updating
 */
on<Moved>({ from.regionLevel != to.regionLevel }) { player: Player ->
    playerRegions[player.index - 1] = to.regionLevel.id
}

on<Moved>({ it.networked && needsRegionChange(it) }, Priority.HIGH) { player: Player ->
    updateRegion(player, false, crossedDynamicBoarder(player))
}

on<World, ReloadZone> {
    players.forEach { player ->
        if (player.networked && inViewOfZone(player, zone)) {
            updateRegion(player, initial = false, force = true)
        }
    }
}

fun needsRegionChange(player: Player) = !inViewOfZone(player, player.viewport!!.lastLoadZone) || crossedDynamicBoarder(player)

fun inViewOfZone(player: Player, zone: Zone): Boolean {
    val viewport = player.viewport!!
    val radius: Int = viewport.zoneRadius - 2
    return Distance.within(player.tile.zone.x, player.tile.zone.y, zone.x, zone.y, radius)
}

fun crossedDynamicBoarder(player: Player) = player.viewport!!.dynamic != inDynamicView(player)

fun inDynamicView(player: Player): Boolean = dynamicZones.isDynamic(player.tile.region)

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
    viewport.lastLoadZone = player.tile.zone
}

fun update(player: Player, initial: Boolean, force: Boolean) {
    val viewport = player.viewport ?: return
    val xteaList = mutableListOf<IntArray>()

    val zone = player.tile.zone
    val zoneX = zone.x
    val zoneY = zone.y

    val radius = viewport.zoneRadius
    for (regionX in (zone.x - radius) / 8..(zone.x + radius) / 8) {
        for (regionY in (zone.y - radius) / 8..(zone.y + radius) / 8) {
            val xtea = blankXtea
            xteaList.add(xtea)
        }
    }

    viewport.dynamic = false

    player.client?.mapRegion(
        zoneX = zoneX,
        zoneY = zoneY,
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
    val zones = mutableListOf<Int?>()

    val view = player.tile.zone.minus(viewport.zoneRadius, viewport.zoneRadius)
    val zoneSize = viewport.zoneArea
    var append = 0
    for (origin in view.toCuboid(zoneSize, zoneSize).copy(minLevel = 0, maxLevel = 3).toZones()) {
        val target = dynamicZones.getDynamicZone(origin)
        if (target == null) {
            zones.add(null)
            continue
        }
        zones.add(target)
        val xtea = blankXtea
        if (!xteaList.contains(xtea)) {
            xteaList.add(xtea)
        } else {
            append++
        }
    }
    for (i in 0 until append) {
        xteaList.add(blankXtea)
    }
    viewport.dynamic = true
    player.client?.dynamicMapRegion(
        zoneX = player.tile.zone.x,
        zoneY = player.tile.zone.y,
        forceRefresh = force,
        mapSize = Viewport.VIEWPORT_SIZES.indexOf(viewport.tileSize),
        zones = zones,
        xteas = xteaList.toTypedArray(),
        clientIndex = if (initial) player.index - 1 else null,
        playerRegions = if (initial) playerRegions else null,
        clientTile = if (initial) player.tile.id else null
    )
}