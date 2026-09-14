package content.skill.constitution.drink

import content.entity.player.dialogue.type.item
import world.gregs.voidps.engine.Script

class CadavaPotion : Script {

    init {
        itemOption("Drink", "cadava_potion") {
            item("cadava_potion", "You dare not drink.")
        }
    }
}
