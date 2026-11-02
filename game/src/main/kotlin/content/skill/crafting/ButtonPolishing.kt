package content.skill.crafting

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.network.login.protocol.encode.playSoundEffect

class ButtonPolishing : Script {

    init {
        itemOption("Polish", "buttons") { (item, slot) ->
            if (!has(Skill.Crafting, 3)) {
                message("You rub the buttons on your clothes but they aren't improved by the process")
                return@itemOption
            }
            if (inventory.replace(slot, item.id, "polished_buttons")) {
                message("You rub the buttons on your clothes and they become more shiny.")
                client?.playSoundEffect(3281)
                exp(Skill.Crafting, 5.0)
            }
        }
    }
}
