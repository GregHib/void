package content.area.misthalin.lumbridge.church

import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.chat.notEnough
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class GravestoneShop : Script {

    init {
        interfaceOpened("gravestone_shop") { id ->
            sendVariable("gravestone_current")
            if (questCompleted("the_restless_ghost")) {
                addVarbit("unlocked_gravestones", "flag")
                addVarbit("unlocked_gravestones", "small_gravestone")
                addVarbit("unlocked_gravestones", "ornate_gravestone")
            }
            if (questCompleted("the_giant_dwarf")) {
                addVarbit("unlocked_gravestones", "font_of_life")
                addVarbit("unlocked_gravestones", "stele")
                addVarbit("unlocked_gravestones", "symbol_of_saradomin")
                addVarbit("unlocked_gravestones", "symbol_of_zamorak")
                addVarbit("unlocked_gravestones", "symbol_of_guthix")
                addVarbit("unlocked_gravestones", "angel_of_death")
                if (questCompleted("land_of_the_goblins")) {
                    addVarbit("unlocked_gravestones", "symbol_of_bandos")
                }
                if (questCompleted("temple_of_ikov")) {
                    addVarbit("unlocked_gravestones", "symbol_of_armadyl")
                }
                if (questCompleted("desert_treasure")) {
                    addVarbit("unlocked_gravestones", "ancient_symbol")
                }
            }
            if (questCompleted("king_of_the_dwarves")) {
                addVarbit("unlocked_gravestones", "royal_dwarven_gravestone")
            }
            interfaceOptions.unlockAll(id, "button", 0 until 13)
        }

        interfaceOption(id = "gravestone_shop:button") { (_, itemSlot) ->
            val name = EnumDefinitions.get("gravestone_names").getString(itemSlot)
            val id = name.replace(" ", "_").lowercase()
            if (get("gravestone_current", "memorial_plaque") == id) {
                return@interfaceOption
            }
            val cost = EnumDefinitions.get("gravestone_price").getInt(itemSlot)
            if (cost > 0 && !inventory.remove("coins", cost)) {
                notEnough("coins")
                return@interfaceOption
            }
            set("gravestone_current", id)
        }
    }
}
