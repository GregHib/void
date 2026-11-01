package content.area.misthalin.wizards_tower.runecrafting_guild

import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

class RunecraftingGuildMap : Script {

    init {
        objectOperate("Study", "rcguild_guild_table_map") {
            open("rc_guild_map")
            tab(Tab.Inventory)
        }

        itemOnObjectOperate("*", "rcguild_guild_table_map") {
            open("rc_guild_map")
            reveal(it.item)
        }

        interfaceOption("Reveal", "rc_guild_side:*") {
            reveal(it.item)
        }

        interfaceOpened("rc_guild_map") {
            open("rc_guild_side")
            interfaceOptions.send("rc_guild_side", "inventory")
            interfaceOptions.unlockAll("rc_guild_side", "inventory", 0 until 28)
        }

        interfaceClosed("rc_guild_map") {
            close("rc_guild_side")
            open("inventory")
        }
    }

    private fun Player.reveal(item: Item) {
        if (!item.id.endsWith("talisman")) {
            message("You can only use an altar talisman on the map.")
            return
        }
        val type = item.id.removeSuffix("_talisman")
        val elements = setOf("air", "body", "mind", "earth", "water", "fire", "chaos", "law", "blood", "nature", "death", "cosmic")
        for (element in elements) {
            interfaces.sendVisibility("rc_guild_map", element, type == "omni" || type == element)
        }
        val message = Tables.string("rc_guild_map.$type.text")
        interfaces.sendText("rc_guild_map", "info_txt", message)
    }
}
