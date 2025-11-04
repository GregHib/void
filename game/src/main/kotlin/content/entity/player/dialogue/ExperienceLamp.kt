package content.entity.player.dialogue

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.suspend.StringSuspension

class ExperienceLamp : Script {

    init {
        interfaceOption("Select", id = "skill_stat_advance:*") {
            set("stat_advance_selected_skill", it.component)
        }

        interfaceOption("Confirm", "skill_stat_advance:confirm") {
            (dialogueSuspension as? StringSuspension)?.resume(get("stat_advance_selected_skill", "none"))
        }
    }
}
