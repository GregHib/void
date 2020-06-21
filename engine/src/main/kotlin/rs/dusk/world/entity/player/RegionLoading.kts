import rs.dusk.engine.client.session.send
import rs.dusk.engine.client.verify.verify
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.engine.Tick
import rs.dusk.engine.model.entity.Deregistered
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.index.Character
import rs.dusk.engine.model.entity.index.Moved
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerRegistered
import rs.dusk.engine.model.entity.index.player.Players
import rs.dusk.engine.model.entity.item.FloorItems
import rs.dusk.engine.model.entity.item.offset
import rs.dusk.engine.model.entity.list.MAX_PLAYERS
import rs.dusk.engine.model.world.ChunkPlane
import rs.dusk.engine.model.world.Region
import rs.dusk.engine.model.world.map.MapReader
import rs.dusk.engine.model.world.map.location.Xtea
import rs.dusk.engine.model.world.map.location.Xteas
import rs.dusk.network.rs.codec.game.decode.message.RegionLoadedMessage
import rs.dusk.network.rs.codec.game.encode.message.ChunkMessage
import rs.dusk.network.rs.codec.game.encode.message.FloorItemAddMessage
import rs.dusk.network.rs.codec.game.encode.message.MapRegionMessage
import rs.dusk.network.rs.codec.login.decode.message.GameLoginMessage
import rs.dusk.utility.inject
import kotlin.math.abs

val xteas: Xteas by inject()
val players: Players by inject()
val bus: EventBus by inject()
val maps: MapReader by inject()
val items: FloorItems by inject()

val regions = IntArray(MAX_PLAYERS - 1)

PlayerRegistered then {
    regions[player.index - 1] = player.tile.regionPlane.id
}

Deregistered where { entity is Player } then {
    val player = entity as Player
    regions[player.index - 1] = 0
}

GameLoginMessage verify { player ->
    calculateRegions(player, true)
    bus.emit(PlayerRegistered(player))
    bus.emit(Registered(player))
}

RegionLoadedMessage verify { player ->
    player.viewport.loaded = true
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

fun calculateRegions(player: Player, initial: Boolean) {
    val regions = player.viewport.regions
    val size = player.viewport.size shr 4
    val chunk = player.tile.chunk
    regions.clear()
    for (regionX in chunk.x.nearby(size)) {
        for (regionY in chunk.y.nearby(size)) {
            regions.add(Region.getId(regionX, regionY))
        }
    }
    update(player, initial)
    player.viewport.lastLoadChunk = player.tile.chunk
}

fun update(player: Player, initial: Boolean) {
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

    player.viewport.regions.forEach { regionId ->
        val chunk = Region(regionId).chunk
        // TODO cap min and max?
        for (x in chunk.x until chunk.x + 8) {
            for (y in chunk.y until chunk.y + 8) {
                val chunkPlane = ChunkPlane(x, y, player.tile.plane)
                val viewChunkSize = player.viewport.size shr 4
                val updates = items.chunks[chunkPlane.id] ?: return
                val base = player.viewport.lastLoadChunk.minus(viewChunkSize, viewChunkSize)
                val chunkOffset = player.tile.chunk.minus(base)
                player.send(ChunkMessage(chunkOffset.x, chunkOffset.y, player.tile.plane))
                updates.forEach { item ->
                    player.send(FloorItemAddMessage(item.tile.offset(), item.id, item.amount))
                }
            }
        }
    }
}

Tick then {
    items.updates.forEach { (chunkPlane, updates) ->
        val region = chunkPlane.region.id
        players.indexed.filter { it != null && it.viewport.regions.contains(region) }.filterNotNull().forEach { player ->
            val viewChunkSize = player.viewport.size shr 4
            val base = player.viewport.lastLoadChunk.minus(viewChunkSize, viewChunkSize)
            val chunkOffset = player.tile.chunk.minus(base)
            player.send(ChunkMessage(chunkOffset.x, chunkOffset.y, player.tile.plane))
            updates.forEach { message ->
                player.send(message)
            }
        }
    }
    items.updates.clear()
}