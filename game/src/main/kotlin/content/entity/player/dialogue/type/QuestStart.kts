package content.entity.player.dialogue.type

import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.suspend.StringSuspension

interfaceOption("Show required items", "items_hidden_button_txt", "quest_intro") {
    player.interfaces.sendVisibility(id, "items_hide_show_layer", false)
    player.interfaces.sendVisibility(id, "items_text_details_layer", true)
    player.interfaces.sendVisibility(id, "scroll_layer_item", true)
}

interfaceOption("Show rewards", "hidden_button_txt", "quest_intro") {
    player.interfaces.sendVisibility(id, "hide_show_layer", false)
    player.interfaces.sendVisibility(id, "text_details_layer", true)
    player.interfaces.sendVisibility(id, "scroll_layer_rewards", true)
}

interfaceOption("Mark", "objective_set", "quest_intro") {
    player["quest_intro_unmark_map"] = !player["quest_intro_unmark_map", false]
}

interfaceOption("Mark", "objective_text", "quest_intro") {
    player["quest_intro_unmark_map"] = !player["quest_intro_unmark_map", false]
}

interfaceOption("No", "startno_layer", "quest_intro") {
    (player.dialogueSuspension as? StringSuspension)?.resume("no")
}

interfaceOption("Yes", "startyes_layer", "quest_intro") {
    (player.dialogueSuspension as? StringSuspension)?.resume("yes")
}

interfaceClose("quest_intro") { player ->
    (player.dialogueSuspension as? StringSuspension)?.resume("no")
}
