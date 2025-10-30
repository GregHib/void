package content.skill.crafting

import content.entity.player.inv.item.ItemUsedOnItem
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.onEvent
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Thread : Script {

    init {
        onEvent<Player, ItemUsedOnItem>("item_used_on_item", Skill.Crafting) { player ->
            if (def.add.any { it.id == "thread" }) {
                val value = player.inc("thread_used")
                if (value == 5) {
                    player.clear("thread_used")
                    player.inventory.remove("thread")
                    player.message("You use up one of your reels of thread.")
                }
            }
        }
    }
}
