package world.gregs.voidps.engine.client.update.task

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import kotlinx.io.pool.DefaultPool
import org.koin.dsl.module
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Viewport
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.engine.map.PooledIdMap
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.network.chunk.ChunkUpdate
import world.gregs.voidps.network.chunk.ChunkUpdateEncoder
import world.gregs.voidps.network.encode.clearChunk

/**
 * Groups messages by [Chunk] to send to all subscribed [Player]s
 * Batched messages are sent and cleared at the end of the tick
 * Initial messages are stored until removed and sent on subscription
 */
class ChunkBatches(
    private val encoders: ChunkUpdateEncoder = ChunkUpdateEncoder()
) : Runnable {
    private val batches = PooledIdMap<MutableCollection<ChunkUpdate>, ChunkUpdate, Chunk>(
        pool = object : DefaultPool<MutableCollection<ChunkUpdate>>(MAX_PLAYERS) {
            override fun produceInstance() = ObjectLinkedOpenHashSet<ChunkUpdate>(EXPECTED_UPDATES)
        }
    )
    private val initials = PooledIdMap<MutableCollection<ChunkUpdate>, ChunkUpdate, Chunk>(
        pool = object : DefaultPool<MutableCollection<ChunkUpdate>>(MAX_PLAYERS) {
            override fun produceInstance() = ObjectLinkedOpenHashSet<ChunkUpdate>(EXPECTED_UPDATES)
        }
    )

    /**
     * Returns the chunk offset for [chunk] relative to [player]'s viewport
     */
    private fun getChunkOffset(viewport: Viewport, chunk: Chunk): Chunk {
        val base = viewport.lastLoadChunk.minus(viewport.chunkRadius, viewport.chunkRadius)
        return chunk.minus(base)
    }

    /**
     * Adds [message] to the batch update for [chunk]
     */
    fun update(chunk: Chunk, message: ChunkUpdate) {
        batches.add(chunk, message)
    }

    /**
     * Adds initial messages for a chunk
     */
    fun addInitial(chunk: Chunk, message: ChunkUpdate) {
        initials.add(chunk, message)
    }

    /**
     * Removes an initial messages for a chunk
     */
    fun removeInitial(chunk: Chunk, message: ChunkUpdate) {
        initials[chunk]?.remove(message)
    }

    fun run(player: Player) {
        val previous = player.tile.minus(player.movement.delta).chunk.toChunkCuboid( player.viewport!!.localRadius)
        forEachChunk(player, player.tile) { chunk ->
            if (previous.contains(chunk.x, chunk.y, chunk.plane)) {
                encode(player, chunk, batches[chunk] ?: return@forEachChunk)
            } else {
                sendChunkClear(player, chunk)
                encode(player, chunk, initials[chunk] ?: return@forEachChunk)
            }
        }
    }

    /**
     * Sends clear message for [chunk] to [player]
     */
    private fun sendChunkClear(player: Player, chunk: Chunk) {
        val chunkOffset = getChunkOffset(player.viewport!!, chunk)
        player.client?.clearChunk(chunkOffset.x, chunkOffset.y, chunk.plane)
    }

    private fun forEachChunk(player: Player, tile: Tile, block: (Chunk) -> Unit) {
        val area = tile.chunk.toCuboid(radius = player.viewport!!.localRadius)
        val max = Tile(area.maxX, area.maxY, area.maxPlane).chunk
        val min = Tile(area.minX, area.minY, area.minPlane).chunk
        for (x in min.x..max.x) {
            for (y in min.y..max.y) {
                block(Chunk(x, y, tile.plane))
            }
        }
    }

    /**
     * Sends all chunk batches to subscribers
     */
    override fun run() {
        batches.forEach { (_, messages) ->
            messages.clear()
        }
    }

    private fun encode(player: Player, chunk: Chunk, messages: Collection<ChunkUpdate>) {
        val client = player.client ?: return
        val visible = messages.filter { it.visible(player.name) }
        if (visible.isEmpty()) {
            return
        }
        val offset = getChunkOffset(player.viewport!!, chunk)
        encoders.encode(client, messages, offset.x, offset.y, chunk.plane)
    }

    companion object {
        private const val EXPECTED_PLAYERS_PER_CHUNK = 8
        private const val EXPECTED_UPDATES = EXPECTED_PLAYERS_PER_CHUNK * 2
    }
}

val batchedChunkModule = module {
    single { ChunkBatches() }
}