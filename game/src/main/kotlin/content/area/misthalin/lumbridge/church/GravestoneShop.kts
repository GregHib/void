package content.area.misthalin.lumbridge.church

import content.quest.questCompleted
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.chat.notEnough
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

val enums: EnumDefinitions by inject()

interfaceOpen("gravestone_shop") { player ->
    player.sendVariable("gravestone_current")
    if (player.questCompleted("the_restless_ghost")) {
        player.addVarbit("unlocked_gravestones", "flag")
        player.addVarbit("unlocked_gravestones", "small_gravestone")
        player.addVarbit("unlocked_gravestones", "ornate_gravestone")
    }
    if (player.questCompleted("the_giant_dwarf")) {
        player.addVarbit("unlocked_gravestones", "font_of_life")
        player.addVarbit("unlocked_gravestones", "stele")
        player.addVarbit("unlocked_gravestones", "symbol_of_saradomin")
        player.addVarbit("unlocked_gravestones", "symbol_of_zamorak")
        player.addVarbit("unlocked_gravestones", "symbol_of_guthix")
        player.addVarbit("unlocked_gravestones", "angel_of_death")
        if (player.questCompleted("land_of_the_goblins")) {
            player.addVarbit("unlocked_gravestones", "symbol_of_bandos")
        }
        if (player.questCompleted("temple_of_ikov")) {
            player.addVarbit("unlocked_gravestones", "symbol_of_armadyl")
        }
        if (player.questCompleted("desert_treasure")) {
            player.addVarbit("unlocked_gravestones", "ancient_symbol")
        }
    }
    if (player.questCompleted("king_of_the_dwarves")) {
        player.addVarbit("unlocked_gravestones", "royal_dwarven_gravestone")
    }
    player.interfaceOptions.unlockAll(id, "button", 0 until 13)
}

interfaceOption("*", "button", "gravestone_shop") {
    val name = enums.get("gravestone_names").getString(itemSlot)
    val id = name.replace(" ", "_").lowercase()
    if (player["gravestone_current", "memorial_plaque"] == id) {
        return@interfaceOption
    }
    val cost = enums.get("gravestone_price").getInt(itemSlot)
    if (cost > 0 && !player.inventory.remove("coins", cost)) {
        player.notEnough("coins")
        return@interfaceOption
    }
    player["gravestone_current"] = id
}
