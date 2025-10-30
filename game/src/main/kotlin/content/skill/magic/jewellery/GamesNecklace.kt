package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.inventoryItem
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.inject

class GamesNecklace : Script {

    val areas: AreaDefinitions by inject()

    val burthorpe = areas["burthorpe_teleport"]
    val barbarianOutput = areas["barbarian_outpost_teleport"]
    val clanWars = areas["clan_wars_teleport"]
    val wildernessVolcano = areas["wilderness_volcano_teleport"]
    val burghDeRott = areas["burgh_de_rott_teleport"]

    init {
        inventoryItem("Rub", "games_necklace_#", "inventory") {
            if (player.contains("delay")) {
                return@inventoryItem
            }
            choice("Where would you like to teleport to?") {
                option("Burthorpe Games Rooms.") {
                    jewelleryTeleport(player, inventory, slot, burthorpe)
                }
                option("Barbarian Outpost.") {
                    jewelleryTeleport(player, inventory, slot, barbarianOutput)
                }
                option("Clan Wars.") {
                    jewelleryTeleport(player, inventory, slot, clanWars)
                }
                option("Wilderness Volcano.") {
                    jewelleryTeleport(player, inventory, slot, wildernessVolcano)
                }
                option("Burgh De Rott.", { player.questCompleted("darkness_of_hallowvale") }) {
                    jewelleryTeleport(player, inventory, slot, burghDeRott)
                }
                option("Nowhere.", { !player.questCompleted("darkness_of_hallowvale") })
            }
        }

        inventoryItem("*", "games_necklace_#", "worn_equipment") {
            if (player.contains("delay")) {
                return@inventoryItem
            }
            val area = when (option) {
                "Burthorpe" -> burthorpe
                "Barbarian Outpost" -> barbarianOutput
                "Clan Wars" -> clanWars
                "Wilderness Volcano" -> wildernessVolcano
                "Burgh De Rott" -> {
                    if (!player.questCompleted("darkness_of_hallowvale")) {
                        statement("You need to have completed The Darkness of Hallowvale quest to teleport to this location.")
                        return@inventoryItem
                    }
                    burghDeRott
                }
                else -> return@inventoryItem
            }
            jewelleryTeleport(player, inventory, slot, area)
        }
    }
}
