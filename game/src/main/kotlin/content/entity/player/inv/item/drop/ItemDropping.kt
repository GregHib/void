package content.entity.player.inv.item.drop

import com.github.michaelbull.logging.InlineLogger
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.inDungeoneering
import content.entity.player.inv.item.tradeable
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.Items
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class ItemDropping : Script {

    init {
        itemOption("Drop") { (item, slot) ->
            drop(this, item, slot)
        }
    }

    companion object {
        private val logger = InlineLogger()

        fun drop(player: Player, item: Item, slot: Int) {
            player.queue.clearWeak()
            if (!Items.droppable(player, item)) {
                return
            }
            AuditLog.event(player, "dropped", item, player.tile)
            if (player.inventory.remove(slot, item.id, item.amount)) {
                if (player.inDungeoneering) {
                    FloorItems.add(player.tile, item.id, item.amount, revealTicks = 0, disappearTicks = FloorItems.NEVER, owner = player)
                } else {
                    if (item.tradeable) {
                        FloorItems.add(player.tile, item.id, item.amount, revealTicks = 100, disappearTicks = 200, owner = player)
                    } else {
                        FloorItems.add(player.tile, item.id, item.amount, charges = item.charges(), revealTicks = FloorItems.NEVER, disappearTicks = 300, owner = player)
                    }
                }
                Items.drop(player, item)
                player.sound("drop_item")
            } else {
                logger.info { "Error dropping item $item for $player" }
            }
        }
    }
}
