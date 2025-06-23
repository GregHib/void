package content.area.misthalin.barbarian_village.stronghold_of_security

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.inv.inventoryItem
import content.skill.magic.spell.Teleport
import content.skill.magic.spell.teleportTakeOff
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

val logger = InlineLogger()

inventoryItem("Invoke", "skull_sceptre") {
    Teleport.teleport(player, Tile(3081, 3421), "skull_sceptre")
}

teleportTakeOff("skull_sceptre") {
    if (player.equipped(EquipSlot.Weapon).id == "skull_sceptre") {
        if (!player.equipment.discharge(player, EquipSlot.Weapon.index, 1)) {
            logger.warn { "Failed to discharge skull sceptre for $player" }
            cancel()
        }
        return@teleportTakeOff
    }
    val index = player.inventory.indexOf("skull_sceptre")
    if (index == -1) {
        logger.warn { "Failed to find skull sceptre for $player" }
        cancel()
        return@teleportTakeOff
    }
    if (!player.inventory.discharge(player, index, 1)) {
        logger.warn { "Failed to discharge skull sceptre for $player" }
        cancel()
    }
}

inventoryItem("Divine", "skull_sceptre") {
    val charges = item.charges()
    // TODO proper message
    player.message("The sceptre has $charges ${"charge".plural(charges)} remaining.")
}
