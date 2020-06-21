import rs.dusk.engine.client.verify.verify
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.index.Character
import rs.dusk.engine.model.entity.index.Moved
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerRegistered
import rs.dusk.engine.model.world.Region
import rs.dusk.engine.model.world.map.MapReader
import rs.dusk.network.rs.codec.game.decode.message.RegionLoadedMessage
import rs.dusk.network.rs.codec.login.decode.message.GameLoginMessage
import rs.dusk.utility.inject
import rs.dusk.world.entity.player.map.RegionChanged
import rs.dusk.world.entity.player.map.RegionLoaded
import kotlin.math.abs

/**
 * Keeps track of when players enter and move between regions
 * Loads maps when they are accessed
 * Emits [RegionChanged] events when a players region has changed
 */

val bus: EventBus by inject()
val maps: MapReader by inject()

GameLoginMessage verify { player ->
    calculateRegions(player, true)
    bus.emit(PlayerRegistered(player))
    bus.emit(Registered(player))
}

RegionLoadedMessage verify { player ->
    player.viewport.loaded = true
    bus.emit(RegionLoaded(player))
    player.viewport.regionsLoading.clear()
}

Moved where { entity is Player && needsRegionChange(entity) } then {
    calculateRegions(entity as Player, false)
}

Registered where { entity is Character } then {
    maps.load(entity.tile.region)
}

Moved then {
    maps.load(entity.tile.region)
}

fun Int.nearby(size: Int): IntRange {
    return (this - size) / 8..(this + size) / 8
}

fun needsRegionChange(player: Player): Boolean {
    val size: Int = (player.viewport.size shr 3) / 2 - 1
    val delta = player.viewport.lastLoadChunk.delta(player.tile.chunk)
    return abs(delta.x) >= size || abs(delta.y) >= size
}

val regions = mutableSetOf<Int>()

fun calculateRegions(player: Player, initial: Boolean) {
    val regions = player.viewport.regions
    val loading = player.viewport.regionsLoading
    val size = player.viewport.size shr 4
    val chunk = player.tile.chunk
    this.regions.clear()
    this.regions.addAll(regions)
    regions.clear()
    for (regionX in chunk.x.nearby(size)) {
        for (regionY in chunk.y.nearby(size)) {
            val regionId = Region.getId(regionX, regionY)
            regions.add(regionId)
            if(!this.regions.contains(regionId)) {
                loading.add(regionId)
            }
        }
    }
    bus.emit(RegionChanged(player, initial))
    player.viewport.lastLoadChunk = player.tile.chunk
}