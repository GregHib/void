package rs.dusk.engine.model.world.map.chunk

import org.koin.dsl.module
import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.client.session.send
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.world.Chunk
import rs.dusk.engine.model.world.ChunkPlane
import rs.dusk.network.rs.codec.game.encode.message.ChunkUpdateMessage

/**
 * Groups outbound batch messages by [ChunkPlane] sending batched to all subscribed players.
 * TODO
 *  Subscribe to all chunks in region when login
 *  Subscribe sending additions crash client because sent before region load message received?
 *  Subscribe to new chunks when moving (can cover login with this too?)
 *  Unsubscribe from old chunks when moving
 *  Publish messages from floor item system
 *  Add addition messages from floor item system
 *  Remove addition message from floor item system
 */
class BatchedChunks {
    val subscribers = mutableMapOf<ChunkPlane, MutableSet<(ChunkPlane, MutableList<Message>) -> Unit>>()
    val subscriptions = mutableMapOf<Player, (ChunkPlane, MutableList<Message>) -> Unit>()
    val creation = mutableMapOf<ChunkPlane, MutableList<Message>>()
    val batches = mutableMapOf<ChunkPlane, MutableList<Message>>()

    fun createSubscription(player: Player): (ChunkPlane, MutableList<Message>) -> Unit = { chunk, messages ->
        sendChunk(player, chunk)
        messages.forEach { message ->
            player.send(message)
        }
    }

    fun getChunkOffset(player: Player, chunkPlane: ChunkPlane): Chunk {
        val viewChunkSize = player.viewport.size shr 4
        val base = player.viewport.lastLoadChunk.minus(viewChunkSize, viewChunkSize)
        return chunkPlane.chunk.minus(base)
    }

    fun sendChunk(player: Player, chunkPlane: ChunkPlane) {
        val chunkOffset = getChunkOffset(player, chunkPlane)
        player.send(ChunkUpdateMessage(chunkOffset.x, chunkOffset.y, chunkPlane.plane))
    }

    fun sendChunkClear(player: Player, chunkPlane: ChunkPlane) {
        val chunkOffset = getChunkOffset(player, chunkPlane)
        player.send(ChunkUpdateMessage(chunkOffset.x, chunkOffset.y, chunkPlane.plane))
    }

    fun subscribe(player: Player, chunk: ChunkPlane) {
        val subscription = getSubscription(player)
        val subscribers = getSubscribers(chunk)
        if (subscribers.add(subscription)) {
            val messages = creation[chunk] ?: return
            subscription.invoke(chunk, messages)
        }
    }

    fun unsubscribe(player: Player, chunk: ChunkPlane) {
        val subscription = getSubscription(player)
        val subscribers = getSubscribers(chunk)
        if (subscribers.remove(subscription)) {
            sendChunkClear(player, chunk) // FIXME floor item zones aren't cleared.
        }
    }

    fun getSubscription(player: Player) = subscriptions.getOrPut(player) { createSubscription(player) }

    fun getSubscribers(chunk: ChunkPlane) = subscribers.getOrPut(chunk) { mutableSetOf() }

    fun getBatch(chunk: ChunkPlane) = batches.getOrPut(chunk) { mutableListOf() }

    fun getCreation(chunk: ChunkPlane) = creation.getOrPut(chunk) { mutableListOf() }

    fun tick() {
        batches.forEach { (chunk, messages) ->
            if (messages.isNotEmpty()) {
                subscribers[chunk]?.forEach { subscriber ->
                    subscriber.invoke(chunk, messages)
                }
                messages.clear()
            }
        }
    }

    fun publish(chunk: ChunkPlane, message: Message) {
        val batch = getBatch(chunk)
        batch.add(message)
    }

    fun addCreation(chunk: ChunkPlane, message: Message) {
        val batch = getCreation(chunk)
        batch.add(message)
    }

    fun removeCreation(chunk: ChunkPlane, message: Message) {
        val batch = getCreation(chunk)
        batch.remove(message)
    }
}

val batchedChunkModule = module {
    single { BatchedChunks() }
}