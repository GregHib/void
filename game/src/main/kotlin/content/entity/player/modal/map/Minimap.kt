package content.entity.player.modal.map

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class Minimap : Script {

    init {
        interfaceOpen("health_orb") {
            set("life_points", levels.get(Skill.Constitution))
            sendVariable("poisoned")
        }

        interfaceOpen("summoning_orb") {
            sendVariable("show_summoning_orb")
        }
    }
}
