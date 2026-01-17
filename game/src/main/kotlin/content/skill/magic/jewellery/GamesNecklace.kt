package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.AreaDefinitions

class GamesNecklace : Script {

    init {
        itemOption("Rub", "games_necklace_#") {
            if (contains("delay")) {
                return@itemOption
            }
            choice("Where would you like to teleport to?") {
                option("Burthorpe Games Rooms.") {
                    jewelleryTeleport(this, it.inventory, it.slot, AreaDefinitions["burthorpe_teleport"])
                }
                option("Barbarian Outpost.") {
                    jewelleryTeleport(this, it.inventory, it.slot, AreaDefinitions["barbarian_outpost_teleport"])
                }
                option("Clan Wars.") {
                    jewelleryTeleport(this, it.inventory, it.slot, AreaDefinitions["clan_wars_teleport"])
                }
                option("Wilderness Volcano.") {
                    jewelleryTeleport(this, it.inventory, it.slot, AreaDefinitions["wilderness_volcano_teleport"])
                }
                if (questCompleted("darkness_of_hallowvale")) {
                    option("Burgh De Rott.") {
                        jewelleryTeleport(this, it.inventory, it.slot, AreaDefinitions["burgh_de_rott_teleport"])
                    }
                } else {
                    option("Nowhere.")
                }
            }
        }

        itemOption("*", "games_necklace_#", "worn_equipment") {
            if (contains("delay")) {
                return@itemOption
            }
            val area = when (it.option) {
                "Burthorpe" -> "burthorpe_teleport"
                "Barbarian Outpost" -> "barbarian_outpost_teleport"
                "Clan Wars" -> "clan_wars_teleport"
                "Wilderness Volcano" -> "wilderness_volcano_teleport"
                "Burgh De Rott" -> {
                    if (!questCompleted("darkness_of_hallowvale")) {
                        statement("You need to have completed The Darkness of Hallowvale quest to teleport to this location.")
                        return@itemOption
                    }
                    "burgh_de_rott_teleport"
                }
                else -> return@itemOption
            }
            jewelleryTeleport(this, it.inventory, it.slot, AreaDefinitions[area])
        }
    }
}
