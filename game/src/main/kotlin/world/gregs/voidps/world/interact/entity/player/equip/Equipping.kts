package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.emote
import world.gregs.voidps.engine.entity.character.update.visual.player.flagAppearance
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.EquipType
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message

on<ContainerAction>({ container == "inventory" && (option == "Wield" || option == "Wear") }) { player: Player ->
    val def = item.def

    if (failedToRemoveOtherHand(player, def)) {
        player.message("Your inventory is full.")
        return@on
    }

    player.inventory.swap(slot, player.equipment, def["slot", EquipSlot.None].index)
    player.flagAppearance()
}

on<ContainerAction>({ container == "worn_equipment" && option == "Remove" }) { player: Player ->
    if (!player.equipment.move(slot, player.inventory) && player.equipment.result == ContainerResult.Full) {
        player.inventoryFull()
    }
}

on<Registered> { player: Player ->
    player.events.on<Player, ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index }) {
        updateWeaponEmote(player)
    }
    updateWeaponEmote(player)
}

fun updateWeaponEmote(player: Player) {
    val weapon = player.equipped(EquipSlot.Weapon)
    val anim = weapon.def.getParam(644, 1426)
    player.emote = anim
}

fun failedToRemoveOtherHand(player: Player, item: ItemDefinition): Boolean {
    return isHandSlot(item["slot", EquipSlot.None]) && hasTwoHandedWeapon(player, item) && failedToMoveToInventory(player, item)
}

fun failedToMoveToInventory(player: Player, item: ItemDefinition): Boolean {
    val otherSlot = getOtherHandSlot(item["slot", EquipSlot.None])
    if (player.equipment.isIndexFree(otherSlot.index)) {
        return false
    }
    return !movedEquipmentToInventory(player, otherSlot)
}

fun getOtherHandSlot(slot: EquipSlot) = if (slot == EquipSlot.Shield) EquipSlot.Weapon else EquipSlot.Shield

fun movedEquipmentToInventory(player: Player, oppositeSlot: EquipSlot): Boolean {
    return player.equipment.move(oppositeSlot.index, player.inventory)
}

fun isHandSlot(slot: EquipSlot) = slot == EquipSlot.Weapon || slot == EquipSlot.Shield

fun hasTwoHandedWeapon(player: Player, item: ItemDefinition) =
    isTwoHandedWeapon(item) || holdingTwoHandedWeapon(player)

fun isTwoHandedWeapon(item: ItemDefinition) = item["slot", EquipSlot.None] == EquipSlot.Weapon && item["type", EquipType.None] == EquipType.TwoHanded

fun holdingTwoHandedWeapon(player: Player): Boolean {
    val weapon = player.equipped(EquipSlot.Weapon)
    return weapon.def["type", EquipType.None] == EquipType.TwoHanded
}