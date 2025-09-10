package content.skill.fletching

import content.entity.player.dialogue.type.makeAmount
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Fletching
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.event.Script
@Script
class FletchUnfinished {

    val itemDefinitions: ItemDefinitions by inject()
    
    init {
        itemOnItem("knife", "*logs*") {
            val displayItems = toItem.def.extras?.get("fletchables") as? List<String> ?: return@itemOnItem
            it.weakQueue("fletching_make_dialog") {
                val (selected, amount) = makeAmount(
                    displayItems,
                    type = "Make",
                    maximum = 27,
                    text = "What would you like to fletch?",
                )
                val itemToFletch: Fletching = itemDefinitions.get(selected).getOrNull("fletching_unf") ?: return@weakQueue
                if (!it.has(Skill.Fletching, itemToFletch.level, true)) {
                    return@weakQueue
                }
                fletch(it, selected, itemToFletch, toItem.id, amount)
            }
        }

    }

    @Suppress("UNCHECKED_CAST")
    
    fun fletch(player: Player, addItem: String, addItemDef: Fletching, removeItem: String, amount: Int) {
        if (amount <= 0) {
            player.softTimers.stop("fletching")
            return
        }
    
        if (!player.inventory.contains("knife") || !player.inventory.contains(removeItem)) {
            player.softTimers.stop("fletching")
            return
        }
    
        player.weakQueue("fletching", addItemDef.tick) {
            val success = player.inventory.transaction {
                remove(removeItem)
                add(addItem, addItemDef.makeAmount)
            }
    
            if (!success) {
                return@weakQueue
            }
    
            val itemCreated = getFletched(addItem)
            player.message("You carefully cut the wood into $itemCreated.", ChatType.Game)
            player.experience.add(Skill.Fletching, addItemDef.xp)
            player.anim(addItemDef.animation)
            fletch(player, addItem, addItemDef, removeItem, amount - 1)
        }
    }
    
    fun getFletched(itemName: String): String = when {
        itemName.contains("shortbow", ignoreCase = true) -> "a Shortbow"
        itemName.contains("longbow", ignoreCase = true) -> "a Longbow"
        itemName.contains("stock", ignoreCase = true) -> "a Stock"
        itemName.contains("shaft", ignoreCase = true) -> "Shafts"
        else -> "null"
    }
}
