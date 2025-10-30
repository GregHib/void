package content.entity.world

import content.bot.isBot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.entity.MAX_PLAYERS
import world.gregs.voidps.engine.entity.character.mode.move.ReloadRegion
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.playerDespawn
import world.gregs.voidps.engine.event.onEvent
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.engine.map.zone.RegionLoad
import world.gregs.voidps.engine.map.zone.RegionReload
import world.gregs.voidps.network.client.instruction.FinishRegionLoad
import world.gregs.voidps.network.login.protocol.encode.dynamicMapRegion
import world.gregs.voidps.network.login.protocol.encode.mapRegion
import world.gregs.voidps.type.Distance
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Zone

/**
 * Keeps track of when players enter and move between regions
 * Loads maps when they are accessed
 */

class RegionLoading : Script {

    val players: Players by inject()
    val dynamicZones: DynamicZones by inject()

    val playerRegions = IntArray(MAX_PLAYERS - 1)

    private val blankXtea = IntArray(4)

    init {

        /*
            Player regions
         */

        instruction<FinishRegionLoad> { player ->
            player.viewport?.loaded = true
        }

        moved { player, from ->
            if (from.regionLevel != player.tile.regionLevel) {
                playerRegions[player.index - 1] = player.tile.regionLevel.id
            }
        }

        onEvent<Player, RegionLoad> { player ->
            player.viewport?.seen(player)
            playerRegions[player.index - 1] = player.tile.regionLevel.id
            val viewport = player.viewport ?: return@onEvent
            players.forEach { other ->
                viewport.seen(other)
            }
            updateRegion(player, true, crossedDynamicBoarder(player))
            viewport.players.addSelf(player)
        }

        playerDespawn { player ->
            playerRegions[player.index - 1] = 0
        }

        onEvent<Player, ReloadRegion> { player ->
            if (player.networked && needsRegionChange(player)) {
                updateRegion(player, false, crossedDynamicBoarder(player))
            }
        }

        /*
            Region updating
         */

        onEvent<RegionReload> {
            players.forEach { player ->
                if (player.networked && needsRegionChange(player)) {
                    updateRegion(player, initial = false, force = true)
                }
            }
        }
    }

    fun needsRegionChange(player: Player) = !inViewOfZone(player, player.viewport!!.lastLoadZone) || crossedDynamicBoarder(player)

    fun inViewOfZone(player: Player, zone: Zone): Boolean {
        val viewport = player.viewport!!
        val radius: Int = viewport.zoneRadius - 2
        return Distance.within(player.tile.zone.x, player.tile.zone.y, zone.x, zone.y, radius)
    }

    fun inViewOfRegion(player: Player, region: Region): Boolean {
        val viewport = player.viewport!!
        val radius: Int = viewport.tileSize shr 6
        return Distance.within(player.tile.region.x, player.tile.region.y, region.x, region.y, radius)
    }

    fun crossedDynamicBoarder(player: Player) = player.viewport!!.dynamic != inDynamicView(player) || dynamicZones.dynamicUpdate(player.tile.region)

    fun inDynamicView(player: Player): Boolean = dynamicZones.dynamic(player.tile.region)

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
            clientTile = if (initial) player.tile.id else null,
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
            val target = dynamicZones.dynamicZone(origin)
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
            clientTile = if (initial) player.tile.id else null,
        )
    }
}
