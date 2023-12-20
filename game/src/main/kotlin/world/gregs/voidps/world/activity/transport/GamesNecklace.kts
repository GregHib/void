package world.gregs.voidps.world.activity.transport

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.activity.quest.questComplete
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.player.effect.degrade.Degrade
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption

val areas: AreaDefinitions by inject()

val burthorpe = areas["burthorpe_teleport"]
val barbarianOutput = areas["barbarian_outpost_teleport"]
val clanWars = areas["clan_wars_teleport"]
val wildernessVolcano = areas["wilderness_volcano_teleport"]
val burghDeRott = areas["burgh_de_rott_teleport"]

on<InventoryOption>({ inventory == "inventory" && item.id.startsWith("games_necklace_") && option == "Rub" }) { player: Player ->
    choice("Where would you like to teleport to?") {
        option("Burthorpe Games Rooms.") {
            if (Degrade.discharge(player, inventory, slot)) {
                jewelleryTeleport(player, burthorpe)
            }
        }
        option("Barbarian Outpost.") {
            if (Degrade.discharge(player, inventory, slot)) {
                jewelleryTeleport(player, barbarianOutput)
            }
        }
        option("Clan Wars.") {
            if (Degrade.discharge(player, inventory, slot)) {
                jewelleryTeleport(player, clanWars)
            }
        }
        option("Wilderness Volcano.") {
            if (Degrade.discharge(player, inventory, slot)) {
                jewelleryTeleport(player, wildernessVolcano)
            }
        }
        option("Burgh De Rott.", { player.questComplete("darkness_of_hallowvale") }) {
            if (Degrade.discharge(player, inventory, slot)) {
                jewelleryTeleport(player, burghDeRott)
            }
        }
        option("Nowhere.", { !player.questComplete("darkness_of_hallowvale") })
    }
}

on<InventoryOption>({ inventory == "worn_equipment" && item.id.startsWith("games_necklace_") }) { player: Player ->
    val area = when (option) {
        "Burthorpe" -> burthorpe
        "Barbarian Outpost" -> barbarianOutput
        "Clan Wars" -> clanWars
        "Wilderness Volcano" -> wildernessVolcano
        "Burgh De Rott" -> {
            if (!player.questComplete("darkness_of_hallowvale")) {
                statement("""
                    You need to have completed The Darkness of Hallowvale quest
                    to teleport to this location.
                """.trimIndent())
                return@on
            }
            burghDeRott
        }
        else -> return@on
    }
    if (Degrade.discharge(player, inventory, slot)) {
        jewelleryTeleport(player, area)
    }
}