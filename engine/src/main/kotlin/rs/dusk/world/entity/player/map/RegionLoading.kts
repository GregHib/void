import rs.dusk.engine.client.verify.verify
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.index.Character
import rs.dusk.engine.model.entity.index.Moved
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerRegistered
import rs.dusk.engine.model.world.map.MapReader
import rs.dusk.engine.model.world.view
import rs.dusk.network.rs.codec.game.decode.message.RegionLoadedMessage
import rs.dusk.network.rs.codec.login.decode.message.GameLoginMessage
import rs.dusk.utility.inject
import rs.dusk.world.entity.player.map.RegionLoaded
import rs.dusk.world.entity.player.map.RegionMapUpdate
import kotlin.math.abs

/**
 * Keeps track of when players enter and move between regions
 * Loads maps when they are accessed
 * Emits [RegionMapUpdate] events when a players region has changed
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
    println("Region loaded ${player.viewport.loading}")
    bus.emit(RegionLoaded(player))
    player.viewport.loading.clear()
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

fun needsRegionChange(player: Player): Boolean {
    val size: Int = (player.viewport.size shr 3) / 2 - 1
    val delta = player.viewport.lastLoadChunk.delta(player.tile.chunk)
    return abs(delta.x) >= size || abs(delta.y) >= size
}

private val current = mutableSetOf<Int>()

fun calculateRegions(player: Player, initial: Boolean) {
    val regions = player.viewport.regions
    val loading = player.viewport.loading
    val size = player.viewport.size shr 4
    current.clear()
    current.addAll(regions)
    regions.clear()
    val view = player.tile.chunk.view(size)
    for(chunk in view) {
        if(!current.contains(chunk.region.id)) {
            loading.add(chunk.region.id)
        }
        regions.add(chunk.region.id)
    }
    bus.emit(RegionMapUpdate(player, initial))
    player.viewport.lastLoadChunk = player.tile.chunk
}