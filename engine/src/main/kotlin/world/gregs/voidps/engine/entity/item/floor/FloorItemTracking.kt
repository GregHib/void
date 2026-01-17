package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.network.login.protocol.encode.zone.FloorItemReveal

/**
 * Removes or reveals items once a floor items countdown is complete.
 */
class FloorItemTracking(
    private val items: FloorItems,
    private val batches: ZoneBatchUpdates,
) : Runnable {
    private val removal = mutableListOf<FloorItem>()

    override fun run() {
        for ((_, zone) in items.data) {
            for ((_, list) in zone) {
                for (floorItem in list) {
                    if (floorItem.reveal()) {
                        val player = Players.get(floorItem.owner!!)
                        batches.add(floorItem.tile.zone, FloorItemReveal(floorItem.tile.id, floorItem.def.id, floorItem.amount, player?.index ?: -1))
                        floorItem.owner = null
                    } else if (floorItem.remove()) {
                        removal.add(floorItem)
                    }
                }
            }
        }
        for (item in removal) {
            items.remove(item)
        }
        removal.clear()
    }
}
