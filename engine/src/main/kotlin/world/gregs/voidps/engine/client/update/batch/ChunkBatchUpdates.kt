package world.gregs.voidps.engine.client.update.batch

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.io.pool.DefaultPool
import kotlinx.io.pool.ObjectPool
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.client.variable.getOrNull
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.network.encode.chunk.ChunkUpdate
import world.gregs.voidps.network.encode.clearChunk
import world.gregs.voidps.network.encode.encodeBatch
import world.gregs.voidps.network.encode.send
import world.gregs.voidps.network.encode.sendBatch

/**
 * Groups messages by [Chunk] to send to all subscribed [Player]s
 * Batched messages are sent and cleared at the end of the tick
 * Initial messages are stored until removed and sent on subscription
 */
class ChunkBatchUpdates : Runnable {
    private val batches: MutableMap<Int, MutableList<ChunkUpdate>> = Int2ObjectOpenHashMap()
    private val encoded: MutableMap<Int, ByteArray> = Int2ObjectOpenHashMap()
    private val pool: ObjectPool<MutableList<ChunkUpdate>> = object : DefaultPool<MutableList<ChunkUpdate>>(INITIAL_UPDATE_POOL_SIZE) {
        override fun produceInstance() = ObjectArrayList<ChunkUpdate>()
        override fun clearInstance(instance: MutableList<ChunkUpdate>) = instance.apply { clear() }
    }
    private val senders = mutableListOf<Sender>()

    interface Sender {
        fun send(player: Player, chunk: Chunk)
    }

    fun register(sender: Sender) = senders.add(sender)

    /**
     * Adds [update] to the batch update for [chunk]
     */
    fun add(chunk: Chunk, update: ChunkUpdate) {
        batches.getOrPut(chunk.id) { ObjectArrayList() }.add(update)
    }

    override fun run() {
        for ((chunk, updates) in batches) {
            encoded[chunk] = encodeBatch(updates.filter { !it.private })
        }
    }

    fun run(player: Player) {
        val previousChunk: Chunk? = player.getOrNull("previous_chunk")
        val previous = previousChunk?.toRectangle(radius = player.viewport!!.localRadius)?.toChunks(player.tile.plane)?.toSet()
        player["previous_chunk"] = player.tile.chunk
        for (chunk in player.tile.chunk.toRectangle(radius = player.viewport!!.localRadius).toChunks(player.tile.plane)) {
            val entered = previous == null || !previous.contains(chunk)
            if (entered) {
                player.clearChunk(chunk)
                for (sender in senders) {
                    sender.send(player, chunk)
                }
            }
            val updates = batches[chunk.id]?.filter { it.private && it.visible(player.name) } ?: continue
            if (!entered) {
                player.sendBatch(chunk)
            }
            for (update in updates) {
                player.client?.send(update)
            }
        }
    }

    fun reset() {
        for (value in batches.values) {
            pool.recycle(value)
        }
        batches.clear()
    }

    private fun Player.sendBatch(chunk: Chunk) {
        val encoded = encoded[chunk.id] ?: return
        val chunkOffset = getChunkOffset(viewport!!, chunk)
        client?.sendBatch(encoded, chunkOffset.x, chunkOffset.y, chunk.plane)
    }

    companion object {
        private const val INITIAL_UPDATE_POOL_SIZE = 100

        /**
         * Returns the chunk offset for [chunk] relative to player's [viewport]
         */
        private fun getChunkOffset(viewport: Viewport, chunk: Chunk): Chunk {
            val base = viewport.lastLoadChunk.safeMinus(viewport.chunkRadius, viewport.chunkRadius)
            return chunk.safeMinus(base)
        }

        private fun Player.clearChunk(chunk: Chunk) {
            val chunkOffset = getChunkOffset(viewport!!, chunk)
            client?.clearChunk(chunkOffset.x, chunkOffset.y, chunk.plane)
        }
    }
}