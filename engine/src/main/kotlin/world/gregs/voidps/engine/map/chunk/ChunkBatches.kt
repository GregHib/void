package world.gregs.voidps.engine.map.chunk

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.character.player.Player
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
    private val subscribers = mutableMapOf<Chunk, MutableSet<Player>>()
    private val initials = mutableMapOf<Chunk, MutableList<ChunkUpdate>>()
    private val batches = mutableMapOf<Chunk, MutableList<ChunkUpdate>>()


    /**
     * Returns the chunk offset for [chunk] relative to [player]'s viewport
     */
    private fun getChunkOffset(player: Player, chunk: Chunk): Chunk {
        val viewChunkSize = player.viewport.tileSize shr 4
        val base = player.viewport.lastLoadChunk.minus(viewChunkSize, viewChunkSize)
        return chunk.minus(base)
    }

    /**
     * Subscribes a player to [chunk] for batched updates
     */
    fun subscribe(player: Player, chunk: Chunk): Boolean {
        return subscribers.getOrPut(chunk) { mutableSetOf() }.add(player)
    }

    /**
     * Unsubscribes a player from [chunk]
     */
    fun unsubscribe(player: Player, chunk: Chunk): Boolean {
        return subscribers[chunk]?.remove(player) == true
    }

    /**
     * Adds [message] to the batch update for [chunk]
     */
    fun update(chunk: Chunk, message: ChunkUpdate) {
        batches.getOrPut(chunk) { mutableListOf() }.add(message)
    }

    /**
     * Sends the initial batched messages for [chunk] to [player]
     */
    fun sendInitial(player: Player, chunk: Chunk) {
        sendChunkClear(player, chunk)
        val messages = initials[chunk] ?: return
        encode(player, chunk, messages)
    }

    /**
     * Sends clear message for [chunk] to [player]
     */
    private fun sendChunkClear(player: Player, chunk: Chunk) {
        val chunkOffset = getChunkOffset(player, chunk)
        player.client?.clearChunk(chunkOffset.x, chunkOffset.y, chunk.plane)
    }

    /**
     * Adds initial messages for a chunk
     */
    fun addInitial(chunk: Chunk, message: ChunkUpdate) {
        initials.getOrPut(chunk) { mutableListOf() }.add(message)
    }

    /**
     * Removes an initial messages for a chunk
     */
    fun removeInitial(chunk: Chunk, message: ChunkUpdate) {
        initials[chunk]?.remove(message)
    }

    /**
     * Sends all chunk batches to subscribers
     */
    override fun run() {
        batches.forEach { (chunk, messages) ->
            if (messages.isEmpty()) {
                return@forEach
            }
            subscribers[chunk]?.forEach { subscriber ->
                encode(subscriber, chunk, messages)
            }
            messages.clear()
        }
    }

    private fun encode(player: Player, chunk: Chunk, messages: List<ChunkUpdate>) {
        val client = player.client ?: return
        val visible = messages.filter { it.visible(player.name) }
        if (visible.isEmpty()) {
            return
        }
        val offset = getChunkOffset(player, chunk)
        encoders.encode(client, messages, offset.x, offset.y, chunk.plane)
    }

}

val batchedChunkModule = module {
    single { ChunkBatches() }
}