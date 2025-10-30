package content.entity.player.dialogue

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.suspend.StringSuspension

class ExperienceLamp : Script {

    init {
        interfaceOption("Select", id = "skill_stat_advance") {
            player["stat_advance_selected_skill"] = component
        }

        interfaceOption("Confirm", "confirm", "skill_stat_advance") {
            (player.dialogueSuspension as? StringSuspension)?.resume(player["stat_advance_selected_skill", "none"])
        }
    }
}
