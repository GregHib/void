package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
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
        itemOption("Rub", "games_necklace_#") {
            if (contains("delay")) {
                return@itemOption
            }
            choice("Where would you like to teleport to?") {
                option("Burthorpe Games Rooms.") {
                    jewelleryTeleport(this, it.inventory, it.slot, burthorpe)
                }
                option("Barbarian Outpost.") {
                    jewelleryTeleport(this, it.inventory, it.slot, barbarianOutput)
                }
                option("Clan Wars.") {
                    jewelleryTeleport(this, it.inventory, it.slot, clanWars)
                }
                option("Wilderness Volcano.") {
                    jewelleryTeleport(this, it.inventory, it.slot, wildernessVolcano)
                }
                option("Burgh De Rott.", { questCompleted("darkness_of_hallowvale") }) {
                    jewelleryTeleport(this, it.inventory, it.slot, burghDeRott)
                }
                option("Nowhere.", { !questCompleted("darkness_of_hallowvale") })
            }
        }

        itemOption("*", "games_necklace_#", "worn_equipment") {
            if (contains("delay")) {
                return@itemOption
            }
            val area = when (it.option) {
                "Burthorpe" -> burthorpe
                "Barbarian Outpost" -> barbarianOutput
                "Clan Wars" -> clanWars
                "Wilderness Volcano" -> wildernessVolcano
                "Burgh De Rott" -> {
                    if (!questCompleted("darkness_of_hallowvale")) {
                        statement("You need to have completed The Darkness of Hallowvale quest to teleport to this location.")
                        return@itemOption
                    }
                    burghDeRott
                }
                else -> return@itemOption
            }
            jewelleryTeleport(this, it.inventory, it.slot, area)
        }
    }
}
