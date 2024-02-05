package world.gregs.voidps.world.activity.transport.teleport

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.entity.player.equip.inventory

val areas: AreaDefinitions by inject()

val jalsavrah = areas["jalsavrah_teleport"]
val jaleustrophos = areas["jaleustrophos_teleport"]
val jaldraocht = areas["jaldraocht_teleport"]

inventory({ inventory == "inventory" && item.id.startsWith("pharaohs_sceptre_") && option == "Teleport" }) { player: Player ->
    choice("Which Pyramid do you want to teleport to?") {
        option("Jalsavrah") {
            itemTeleport(player, inventory, slot, jalsavrah, "pharaohs_sceptre")
        }
        option("Jaleustrophos") {
            itemTeleport(player, inventory, slot, jaleustrophos, "pharaohs_sceptre")
        }
        option("Jaldraocht") {
            itemTeleport(player, inventory, slot, jaldraocht, "pharaohs_sceptre")
        }
        option("I'm happy where I am actually.")
    }
}

inventory({ inventory == "worn_equipment" && item.id.startsWith("pharaohs_sceptre_") }) { player: Player ->
    val area = when (option) {
        "Jalsavrah" -> jalsavrah
        "Jaleustrophos" -> jaleustrophos
        "Jaldraocht" -> jaldraocht
        else -> return@inventory
    }
    itemTeleport(player, inventory, slot, area, "pharaohs_sceptre")
}