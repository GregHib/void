package content.achievement

import content.entity.player.dialogue.type.skillLamp
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class AntiqueLamp : Script {

    init {
        itemOption("Rub", "antique_lamp_easy_lumbridge_tasks") { (item, slot) ->
            val skill = skillLamp()
            if (inventory.remove(slot, item.id)) {
                exp(skill, 500.0)
                statement("<blue>Your wish has been granted!<br><black>You have been awarded 500 ${skill.name} experience!")
            }
        }
    }
}
