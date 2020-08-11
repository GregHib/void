package rs.dusk.world.interact.entity.player.equip

import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.entity.character.contain.ContainerResult
import rs.dusk.engine.entity.character.contain.equipment
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerRegistered
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.update.visual.player.emote
import rs.dusk.engine.entity.character.update.visual.player.flagAppearance
import rs.dusk.engine.entity.item.EquipSlot
import rs.dusk.engine.entity.item.EquipType
import rs.dusk.engine.entity.item.detail.ItemDetail
import rs.dusk.engine.entity.item.detail.ItemDetails
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.inject

val itemDetails: ItemDetails by inject()
val itemDecoder: ItemDecoder by inject()

ContainerAction where { container == "inventory" && (option == "Wield" || option == "Wear") } then {
    val details = itemDetails.get(item)

    if (failedToRemoveOtherHand(player, details)) {
        player.message("Your inventory is full.")
        return@then
    }

    player.inventory.switch(slot, player.equipment, details.slot.index)
    player.flagAppearance()
}

ContainerAction where { container == "worn_equipment" && option == "Remove" } then {
    val result = player.equipment.move(slot, player.inventory)
    if (result is ContainerResult.Addition.Failure.Full) {
        player.message("You don't have enough inventory space.")
    }
}

PlayerRegistered then {
    player.equipment.listeners.add { list ->
        for ((index, _, _) in list) {
            if (index == EquipSlot.Weapon.index) {
                val weapon = player.equipment.getItem(EquipSlot.Weapon.index)
                val def = itemDecoder.getSafe(weapon)
                val anim = def.params?.get(644) as? Int ?: 1426
                player.emote = anim
            }
        }
    }
}

fun failedToRemoveOtherHand(player: Player, details: ItemDetail): Boolean {
    return isHandSlot(details.slot) && hasTwoHandedWeapon(player, details) && failedToMoveToInventory(player, details)
}

fun failedToMoveToInventory(player: Player, details: ItemDetail): Boolean {
    val otherSlot = getOtherHandSlot(details.slot)
    if (player.equipment.isIndexFree(otherSlot.index)) {
        return false
    }
    return !movedEquipmentToInventory(player, otherSlot)
}

fun getOtherHandSlot(slot: EquipSlot) = if (slot == EquipSlot.Shield) EquipSlot.Weapon else EquipSlot.Shield

fun movedEquipmentToInventory(player: Player, oppositeSlot: EquipSlot): Boolean {
    val moveResult = player.equipment.move(oppositeSlot.index, player.inventory)
    return moveResult == ContainerResult.Addition.Added
}

fun isHandSlot(slot: EquipSlot) = slot == EquipSlot.Weapon || slot == EquipSlot.Shield

fun hasTwoHandedWeapon(player: Player, details: ItemDetail) =
    isTwoHandedWeapon(details) || holdingTwoHandedWeapon(player)

fun isTwoHandedWeapon(details: ItemDetail) = details.slot == EquipSlot.Weapon && details.type == EquipType.TwoHanded

fun holdingTwoHandedWeapon(player: Player): Boolean {
    val weapon = player.equipment.getItem(EquipSlot.Weapon.index)
    return itemDetails.get(weapon).type == EquipType.TwoHanded
}