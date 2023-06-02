package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.network.encode.chunk.FloorItemReveal

class FloorItemTracking(
    private val items: FloorItems,
    private val batches: ChunkBatchUpdates
) : Runnable {

    private val removal = mutableListOf<FloorItem>()

    override fun run() {
        for ((_, list) in items.data) {
            for (item in list) {
                if (item.owner != 0 && item.revealTimer-- == 0) {
                    batches.add(item.tile.chunk, FloorItemReveal(item.tile.id, item.def.id, item.amount, item.owner))
                    item.owner = 0
                } else if (item.disappearTimer-- == 0) {
                    removal.add(item)
                }
            }
        }
        for (item in removal) {
            items.remove(item)
        }
        removal.clear()
    }
}