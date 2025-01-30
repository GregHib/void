package content.skill.magic.jewellery

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.inject
import content.entity.player.dialogue.type.choice
import content.entity.player.inv.inventoryItem

val areas: AreaDefinitions by inject()

val duelArena = areas["duel_arena_teleport"]
val castleWars = areas["castle_wars_teleport"]
val mobilisingArmies = areas["mobilising_armies_teleport"]
val fistOfGuthix = areas["fist_of_guthix_teleport"]

inventoryItem("Rub", "ring_of_duelling_#", "inventory") {
    if (player.contains("delay")) {
        return@inventoryItem
    }
    choice("Where would you like to teleport to?") {
        option("Al Kharid Duel Arena.") {
            jewelleryTeleport(player, inventory, slot, duelArena)
        }
        option("Castle Wars Arena.") {
            jewelleryTeleport(player, inventory, slot, castleWars)
        }
        option("Mobilising Armies Command Centre.") {
            jewelleryTeleport(player, inventory, slot, mobilisingArmies)
        }
        option("Fist of Guthix.") {
            jewelleryTeleport(player, inventory, slot, fistOfGuthix)
        }
        option("Nowhere.")
    }
}

inventoryItem("*", "ring_of_duelling_#", "worn_equipment") {
    if (player.contains("delay")) {
        return@inventoryItem
    }
    val area = when (option) {
        "Duel Arena" -> duelArena
        "Castle Wars" -> castleWars
        "Mobilising Armies" -> mobilisingArmies
        "Fist of Guthix" -> fistOfGuthix
        else -> return@inventoryItem
    }
    jewelleryTeleport(player, inventory, slot, area)
}