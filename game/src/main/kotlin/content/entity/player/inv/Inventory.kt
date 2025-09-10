package content.entity.player.inv

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.interfaceSwap
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.swap
import world.gregs.voidps.engine.event.Script
@Script
class Inventory {

    val logger = InlineLogger()
    
    init {
        interfaceRefresh("inventory") { player ->
            player.interfaceOptions.unlockAll(id, "inventory", 0 until 28)
            player.interfaceOptions.unlock(id, "inventory", 28 until 56, "Drag")
            player.sendInventory(id)
        }

        interfaceSwap { player ->
            player.queue.clearWeak()
        }

        interfaceSwap("inventory") { player ->
            player.closeInterfaces()
            if (player.mode is CombatMovement) {
                player.mode = EmptyMode
            }
            if (!player.inventory.swap(fromSlot, toSlot)) {
                logger.info { "Failed switching interface items $this" }
            }
        }

        interfaceOption(component = "inventory", id = "inventory") {
            val itemDef = item.def
            val equipOption = when (optionIndex) {
                6 -> itemDef.options.getOrNull(3)
                7 -> itemDef.options.getOrNull(4)
                9 -> "Examine"
                else -> itemDef.options.getOrNull(optionIndex)
            }
            if (equipOption == null) {
                logger.info { "Unknown item option $item $optionIndex" }
                return@interfaceOption
            }
            player.closeInterfaces()
            if (player.mode is CombatMovement) {
                player.mode = EmptyMode
            }
            player.emit(
                InventoryOption(
                    player,
                    id,
                    item,
                    itemSlot,
                    equipOption,
                ),
            )
        }

    }

}
