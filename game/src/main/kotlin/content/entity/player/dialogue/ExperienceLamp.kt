package content.entity.player.dialogue

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.suspend.Suspension

class ExperienceLamp : Script {

    init {
        interfaceOption("Select", id = "skill_stat_advance:*") {
            set("stat_advance_selected_skill", it.component)
        }

        interfaceOption("Confirm", "skill_stat_advance:confirm") {
            (suspension as? Suspension.StringEntry)?.resume(get("stat_advance_selected_skill", "none"))
        }
    }
}
