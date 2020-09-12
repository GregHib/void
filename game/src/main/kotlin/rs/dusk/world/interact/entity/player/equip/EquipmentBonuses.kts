package rs.dusk.world.interact.entity.player.equip

import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.client.variable.*
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.update.visual.player.APPEARANCE_MASK
import rs.dusk.engine.entity.character.update.visual.player.appearance
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.player.display.InterfaceOption

BooleanVariable(4894, Variable.Type.VARBIT, defaultValue = false).register("equipment_banking")
IntVariable(779, Variable.Type.VARC, defaultValue = 1426).register("equipment_emote")
StringVariable(321, Variable.Type.VARCSTR).register("equipment_name")
StringVariable(322, Variable.Type.VARCSTR).register("equipment_titles")
StringVariable(323, Variable.Type.VARCSTR).register("equipment_names")
StringVariable(324, Variable.Type.VARCSTR).register("equipment_stats")

InterfaceOpened where { name == "equipment_bonuses" } then {
    updateEmote(player)
    player.open("equipment_side")
    player.interfaceOptions.unlockAll("equipment_bonuses", "container", 0 until 16)
}

InterfaceOpened where { name == "equipment_side" } then {
    player.interfaceOptions.send("equipment_side", "container")
    player.interfaceOptions.unlockAll("equipment_side", "container", 0 until 28)
}


InterfaceOption where { (name == "equipment_side" || name == "equipment_bonuses") && component == "container" && option == "Stats" } then {
    // TODO show stats
    player.setVar("equipment_name", "One")
    player.setVar("equipment_titles", "Two>")
    player.setVar("equipment_names", "Three>")
    player.setVar("equipment_stats", "Four")
}

/*
    Redirect equipping actions to regular containers
 */
val bus: EventBus by inject()

InterfaceOption where { name == "equipment_side" && component == "container" && option == "Equip" } then {
    bus.emit(ContainerAction(player, "inventory", item, itemIndex, "Wield"))
    checkEmoteUpdate(player)
}

InterfaceOption where { name == "equipment_bonuses" && component == "container" && option == "Remove" } then {
    bus.emit(ContainerAction(player, "worn_equipment", item, itemIndex, "Remove"))
    checkEmoteUpdate(player)
}

InterfaceOption then {
    println(this)
}

fun checkEmoteUpdate(player: Player) {
    if (player.visuals.flagged(APPEARANCE_MASK)) {
        updateEmote(player)
    }
}

fun updateEmote(player: Player) {
    player.setVar("equipment_emote", player.appearance.emote)
}