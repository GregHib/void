package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.network.encode.zone.FloorItemReveal

/**
 * Removes or reveals items once a floor items countdown is complete.
 */
class FloorItemTracking(
    private val items: FloorItems,
    private val players: Players,
    private val batches: ZoneBatchUpdates
) : Runnable {
    private val removal = mutableListOf<FloorItem>()

    override fun run() {
        for ((_, list) in items.data) {
            for (item in list) {
                if (item.reveal()) {
                    val player = players.get(item.owner!!)
                    batches.add(item.tile.zone, FloorItemReveal(item.tile.id, item.def.id, item.amount, player?.index ?: -1))
                    item.owner = null
                } else if (item.remove()) {
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