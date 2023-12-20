package world.gregs.voidps.world.activity.transport

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.entity.player.effect.degrade.Degrade
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption

val areas: AreaDefinitions by inject()

val duelArena = areas["duel_arena_teleport"]
val castleWars = areas["castle_wars_teleport"]
val mobilisingArmies = areas["mobilising_armies_teleport"]
val fistOfGuthix = areas["fist_of_guthix_teleport"]

on<InventoryOption>({ inventory == "inventory" && item.id.startsWith("ring_of_duelling_") && option == "Rub" }) { player: Player ->
    choice("Where would you like to teleport to?") {
        option("Al Kharid Duel Arena.") {
            if (Degrade.discharge(player, inventory, slot)) {
                jewelleryTeleport(player, duelArena)
            }
        }
        option("Castle Wars Arena.") {
            if (Degrade.discharge(player, inventory, slot)) {
                jewelleryTeleport(player, castleWars)
            }
        }
        option("Mobilising Armies Command Centre.") {
            if (Degrade.discharge(player, inventory, slot)) {
                jewelleryTeleport(player, mobilisingArmies)
            }
        }
        option("Fist of Guthix.") {
            if (Degrade.discharge(player, inventory, slot)) {
                jewelleryTeleport(player, fistOfGuthix)
            }
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
    if (Degrade.discharge(player, inventory, slot)) {
        jewelleryTeleport(player, area)
    }
}