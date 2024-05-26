package world.gregs.voidps.world.interact.dialogue

import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.suspend.dialogue.StringSuspension
import world.gregs.voidps.engine.suspend.resumeDialogueSuspension

interfaceOption("Select", id = "skill_stat_advance") {
    player["stat_advance_selected_skill"] = component
}

interfaceOption("Confirm", id = "skill_stat_advance") {
    val suspension = player.dialogueSuspension as? StringSuspension ?: return@interfaceOption
    suspension.string = player["stat_advance_selected_skill", "none"]
    player.resumeDialogueSuspension()
}