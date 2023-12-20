package world.gregs.voidps.world.activity.transport

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption

val areas: AreaDefinitions by inject()

val burthorpe = areas["burthorpe_games_necklace_teleport"]
val barbarianOutput = areas["barbarian_outpost_games_necklace_teleport"]
val clanWars = areas["clan_wars_games_necklace_teleport"]
val wildernessVolcano = areas["wilderness_volcano_games_necklace_teleport"]

on<InventoryOption>({ inventory == "inventory" && item.id.startsWith("games_necklace_") && option == "Rub" }) { player: Player ->
    choice("Where would you like to teleport to?") {
        option("Burthorpe Games Rooms.") {
            teleport(player, "games_necklace", burthorpe)
        }
        option("Barbarian Outpost.") {
            teleport(player, "games_necklace", barbarianOutput)
        }
        option("Clan Wars.") {
            teleport(player, "games_necklace", clanWars)
        }
        option("Wilderness Volcano.") {
            teleport(player, "games_necklace", wildernessVolcano)
        }
        option("Nowhere.")
    }
}