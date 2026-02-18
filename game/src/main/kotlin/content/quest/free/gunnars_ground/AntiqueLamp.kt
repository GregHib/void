package content.quest.free.gunnars_ground

import content.entity.player.dialogue.type.skillLamp
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class AntiqueLamp : Script {

    init {
        itemOption("Rub", "antique_lamp_gunnars_ground") { (item, slot) ->
            val skill = skillLamp()

            if (this.levels.get(skill) < 5) {
                statement("<red>This skill is not high enough to gain experience from this lamp.")

                return@itemOption
            }

            if (inventory.remove(slot, item.id)) {
                exp(skill, 200.0)
                statement("<blue>Your wish has been granted!<br><black>You have been awarded 200 ${skill.name} experience!")
            }
        }
    }
}
