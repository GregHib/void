package content.minigame.pyramid_plunder

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.inject
import content.entity.player.dialogue.type.choice
import content.entity.player.inv.inventoryItem
import content.skill.magic.jewellery.itemTeleport

val areas: AreaDefinitions by inject()

val jalsavrah = areas["jalsavrah_teleport"]
val jaleustrophos = areas["jaleustrophos_teleport"]
val jaldraocht = areas["jaldraocht_teleport"]

inventoryItem("Teleport", "pharaohs_sceptre_#", "inventory") {
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

inventoryItem("*", "pharaohs_sceptre_#", "worn_equipment") {
    val area = when (option) {
        "Jalsavrah" -> jalsavrah
        "Jaleustrophos" -> jaleustrophos
        "Jaldraocht" -> jaldraocht
        else -> return@inventoryItem
    }
    itemTeleport(player, inventory, slot, area, "pharaohs_sceptre")
}