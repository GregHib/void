package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.interfaceOption

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