package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.ItemOption
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player

class GamesNecklace : Script {

    init {
        itemOption("Rub", "games_necklace_#") {
            if (contains("delay")) {
                return@itemOption
            }
            choice("Where would you like to teleport to?") {
                option("Burthorpe Games Rooms.") {
                    jewelleryTeleport(this, it.inventory, it.slot, Areas["burthorpe_teleport"])
                }
                option("Barbarian Outpost.") {
                    jewelleryTeleport(this, it.inventory, it.slot, Areas["barbarian_outpost_teleport"])
                }
                option("Clan Wars.") {
                    jewelleryTeleport(this, it.inventory, it.slot, Areas["clan_wars_teleport"])
                }
                option("Wilderness Volcano.") {
                    jewelleryTeleport(this, it.inventory, it.slot, Areas["wilderness_volcano_teleport"])
                }
                if (questCompleted("darkness_of_hallowvale")) {
                    option("Burgh De Rott.") {
                        jewelleryTeleport(this, it.inventory, it.slot, Areas["burgh_de_rott_teleport"])
                    }
                } else {
                    option("Nowhere.")
                }
            }
        }

        itemOption("Burthorpe", "games_necklace_#", "worn_equipment", ::teleport)
        itemOption("Barbarian Outpost", "games_necklace_#", "worn_equipment", ::teleport)
        itemOption("Clan Wars", "games_necklace_#", "worn_equipment", ::teleport)
        itemOption("Wilderness Volcano", "games_necklace_#", "worn_equipment", ::teleport)
        itemOption("Burgh De Rott", "games_necklace_#", "worn_equipment", ::teleport)
    }

    private suspend fun teleport(player: Player, option: ItemOption) {
        if (player.contains("delay")) {
            return
        }
        val area = when (option.option) {
            "Burthorpe" -> "burthorpe_teleport"
            "Barbarian Outpost" -> "barbarian_outpost_teleport"
            "Clan Wars" -> "clan_wars_teleport"
            "Wilderness Volcano" -> "wilderness_volcano_teleport"
            "Burgh De Rott" -> {
                if (!player.questCompleted("darkness_of_hallowvale")) {
                    player.statement("You need to have completed The Darkness of Hallowvale quest to teleport to this location.")
                    return
                }
                "burgh_de_rott_teleport"
            }
            else -> return
        }
        jewelleryTeleport(player, option.inventory, option.slot, Areas[area])
    }
}
