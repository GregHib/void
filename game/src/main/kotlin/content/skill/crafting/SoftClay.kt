package content.skill.crafting

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class SoftClay : Script {

    init {
        crafted(Skill.Crafting) { def ->
            if (def.add.any { it.id == "soft_clay" }) {
                message("You now have some soft, workable clay.", ChatType.Filter)
            }
        }
    }
}
