package world.gregs.voidps.engine.client.update.batch

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.client.variable.getOrNull
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.floor.FloorItemState
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.item.floor.offset
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.map.area.Cuboid
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.network.encode.*
import world.gregs.voidps.network.encode.chunk.ChunkUpdate
import world.gregs.voidps.network.encode.chunk.FloorItemAddition
import world.gregs.voidps.network.encode.chunk.ObjectAddition
import world.gregs.voidps.network.encode.chunk.ObjectRemoval

/**
 * Groups messages by [Chunk] to send to all subscribed [Player]s
 * Batched messages are sent and cleared at the end of the tick
 * Initial messages are stored until removed and sent on subscription
 */
class ChunkBatches(
    private val objects: Objects
) : Runnable {
    private val batches: MutableMap<Int, MutableList<ChunkUpdate>> = Int2ObjectOpenHashMap()
    private val encoded: MutableMap<Int, ByteArray> = Int2ObjectOpenHashMap()

    lateinit var floorItems: FloorItems

    /**
     * Adds [update] to the batch update for [chunk]
     */
    fun add(chunk: Chunk, update: ChunkUpdate) {
        batches.getOrPut(chunk.id) { ObjectArrayList() }.add(update)
    }

    override fun run() {
        for ((chunk, updates) in batches) {
            encoded[chunk] = encodeBatch(updates.filter { !it.private() })
        }
    }

    fun run(player: Player) {
        val previousChunk: Chunk? = player.getOrNull("previous_chunk")
        val previous = if (previousChunk != null) toChunkCuboid(previousChunk, player.viewport!!.localRadius) else null
        player["previous_chunk"] = player.tile.chunk

        for (chunk in player.tile.chunk.toRectangle(radius = player.viewport!!.localRadius).toChunks(player.tile.plane)) {
            val entered = previous == null || !previous.contains(chunk.x, chunk.y, chunk.plane)
            if (entered) {
                player.clearChunk(chunk)
                sendInitial(player, chunk)
                player.sendBatch(chunk)
            }
            val updates = batches[chunk.id]?.filter { it.private() } ?: continue
            if (!entered) {
                player.sendChunk(chunk)
            }
            for (update in updates) {
                player.client?.send(update)
            }
        }
    }

    private fun sendInitial(player: Player, chunk: Chunk) {
        for (obj in objects.getRemoved(chunk) ?: emptySet()) {
            player.client?.send(ObjectRemoval(obj.tile.offset(), obj.type, obj.rotation, obj.owner))
        }
        for (obj in objects.getAdded(chunk) ?: emptySet()) {
            player.client?.send(ObjectAddition(obj.def.id, obj.tile.offset(), obj.type, obj.rotation, obj.owner))

        }
        for (item in floorItems[chunk]) {
            if (item.state == FloorItemState.Public || item.state == FloorItemState.Private && item.owner == player.name) {
                player.client?.send(FloorItemAddition(item.def.id, item.amount, item.tile.offset(), item.owner))
            }
        }
    }

    fun reset() {
        batches.clear()
    }

    private fun toChunkCuboid(chunk: Chunk, radius: Int) = Cuboid(chunk.x - radius, chunk.y - radius, chunk.x + radius * 2 + 1, chunk.y + radius * 2 + 1, chunk.plane, chunk.plane)

    private fun Player.sendBatch(chunk: Chunk) {
        val encoded = encoded[chunk.id] ?: return
        val chunkOffset = getChunkOffset(viewport!!, chunk)
        client?.sendBatch(encoded, chunkOffset.x, chunkOffset.y, chunk.plane)
    }

    companion object {
        /**
         * Returns the chunk offset for [chunk] relative to [player]'s viewport
         */
        private fun getChunkOffset(viewport: Viewport, chunk: Chunk): Chunk {
            val base = viewport.lastLoadChunk.safeMinus(viewport.chunkRadius, viewport.chunkRadius)
            return chunk.safeMinus(base)
        }
        private fun Player.clearChunk(chunk: Chunk) {
            val chunkOffset = getChunkOffset(viewport!!, chunk)
            client?.clearChunk(chunkOffset.x, chunkOffset.y, chunk.plane)
        }

        private fun Player.sendChunk(chunk: Chunk) {
            val chunkOffset = getChunkOffset(viewport!!, chunk)
            client?.updateChunk(chunkOffset.x, chunkOffset.y, chunk.plane)
        }
    }
}