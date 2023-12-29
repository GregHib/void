package world.gregs.voidps.world.activity.transport.teleport

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption

val areas: AreaDefinitions by inject()

val duelArena = areas["duel_arena_teleport"]
val castleWars = areas["castle_wars_teleport"]
val mobilisingArmies = areas["mobilising_armies_teleport"]
val fistOfGuthix = areas["fist_of_guthix_teleport"]

on<InventoryOption>({ inventory == "inventory" && item.id.startsWith("ring_of_duelling_") && option == "Rub" }) { player: Player ->
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

on<InventoryOption>({ inventory == "worn_equipment" && item.id.startsWith("ring_of_duelling_") }) { player: Player ->
    val area = when (option) {
        "Duel Arena" -> duelArena
        "Castle Wars" -> castleWars
        "Mobilising Armies" -> mobilisingArmies
        "Fist of Guthix" -> fistOfGuthix
        else -> return@on
    }
    jewelleryTeleport(player, inventory, slot, area)
}