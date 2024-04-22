package world.gregs.voidps.world.activity.skill.runecrafting

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.obj.teleportTakeOff
import world.gregs.voidps.world.interact.entity.sound.playSound

val objectDefinitions: ObjectDefinitions by inject()

val omni = listOf("air", "mind", "water", "earth", "fire", "body", "cosmic", "law", "nature", "chaos", "death", "blood")

playerSpawn { player ->
    if (player.equipped(EquipSlot.Hat).id.endsWith("_tiara")) {
        toggleAltar(player, player.equipped(EquipSlot.Hat), true)
    }
}

itemAdded("*_tiara", EquipSlot.Hat, "worn_equipment") { player ->
    toggleAltar(player, item, true)
}

itemRemoved("*_tiara", EquipSlot.Hat, "worn_equipment") { player ->
    toggleAltar(player, fromItem, false)
}

fun toggleAltar(player: Player, item: Item, unlocked: Boolean) {
    val type = item.id.removeSuffix("_tiara")
    if (type == "omni") {
        for (t in omni) {
            player["${t}_altar_ruins"] = unlocked
        }
    } else {
        player["${type}_altar_ruins"] = unlocked
    }
}

itemOnObjectOperate("*_talisman", "*_altar_ruins") {
    if (target.id != "${item.id.removeSuffix("_talisman")}_altar_ruins") {
        return@itemOnObjectOperate
    }
    val id = target.def.transforms?.getOrNull(1) ?: return@itemOnObjectOperate
    val definition = objectDefinitions.get(id)
    player.message("You hold the ${item.id.toSentenceCase()} towards the mysterious ruins.")
    player.setAnimation("bend_down")
    delay(2)
    player.mode = Interact(player, target, ObjectOption(player, target, definition, "Enter"), approachRange = -1)
}

teleportTakeOff("Enter", "*_altar_ruins_enter") {
    player.clearAnimation()
    player.playSound("teleport")
    player.message("You feel a powerful force talk hold of you...")
}

teleportTakeOff("Enter", "*_altar_portal") {
    player.clearAnimation()
    player.playSound("teleport")
    player.message("You step through the portal...")
}