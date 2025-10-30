package content.entity.player.modal.map

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class Minimap : Script {

    init {
        interfaceOpen("health_orb") { player ->
            player["life_points"] = player.levels.get(Skill.Constitution)
            player.sendVariable("poisoned")
        }

        interfaceOpen("summoning_orb") { player ->
            player.sendVariable("show_summoning_orb")
        }
    }
}
