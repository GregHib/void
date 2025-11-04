package content.skill.crafting

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Thread : Script {

    init {
        crafted(Skill.Crafting) { def ->
            if (def.add.any { it.id == "thread" }) {
                val value = inc("thread_used")
                if (value == 5) {
                    clear("thread_used")
                    inventory.remove("thread")
                    message("You use up one of your reels of thread.")
                }
            }
        }
    }
}
