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
import world.gregs.voidps.world.interact.entity.sound.playSound

fun canWear(option: String) = option == "Wield" || option == "Wear" || option == "Hold" || option == "Equip"

on<ContainerOption>({ container == "inventory" && canWear(option) }) { player: Player ->
    val def = item.def

    if (!player.hasRequirements(def, true)) {
        return@on
    }

    if (failedToRemoveOtherHand(player, def)) {
        player.message("Your inventory is full.")
        return@on
    }

    val slot = def["slot", EquipSlot.None]
    player.inventory.swap(this.slot, player.equipment, slot.index, combine = true)
    player.flagAppearance()
    playEquipSound(player, def, slot)
}

on<ContainerOption>({ container == "worn_equipment" && option == "Remove" }) { player: Player ->
    if (player.equipment.move(slot, player.inventory)) {
        val slot = item.def["slot", EquipSlot.None]
        playEquipSound(player, item.def, slot)
    } else if (player.equipment.result == ContainerResult.Full) {
        player.inventoryFull()
    }
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index }) { player: Player ->
    updateWeaponEmote(player)
}

on<Registered> { player: Player ->
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

fun playEquipSound(player: Player, item: ItemDefinition, slot: EquipSlot) {
    val name = item.name.toLowerCase()
    val material = item["material", "cloth"]
    var sound = when (slot) {
        EquipSlot.Weapon -> when {
            // Might be able to improve using attack strategies
            name.contains("mystic") && name.contains("staff") -> "equip_elemental_staff"
            name.contains("spear") || name.contains("staff") || name.contains("javelin") || name.contains("hasta") || name.contains("halberd") -> "equip_spear"
            name.contains("hammer") || name.contains("maul") -> "equip_hammer"
            name.contains("mace") || name.contains("flail") -> "equip_mace"
            name.contains("bow") || name.contains("knife") || name.contains("dart") || name.contains("thrownaxe") -> "equip_range"
            name.contains("whip") -> "equip_whip"
            name.contains("hatchet") -> "equip_hatchet"
            name.contains("axe") -> "equip_axe"
            name.contains("salamander") -> "equip_salamander"
            name.contains("banner") -> "equip_banner"
            name.contains("blunt") -> "equip_blunt"
            name == "sled" -> "equip_sled"
            name == "dark bow" -> "equip_darkbow"
            name == "silverlight" -> "equip_silverlight"
            else -> if (material == "metal") "equip_sword" else "equip_clothes"
        }
        EquipSlot.Hat -> if (material == "metal") "equip_helm" else "equip_clothes"
        EquipSlot.Chest -> if (material == "metal") "equip_body" else "equip_clothes"
        EquipSlot.Shield -> if (material == "metal") "equip_shield" else "equip_clothes"
        EquipSlot.Legs -> if (material == "metal") "equip_legs" else "equip_clothes"
        EquipSlot.Feet -> if (material == "metal") "equip_feet" else "equip_clothes"
        EquipSlot.Hands -> if (material == "metal") "equip_hands" else "equip_clothes"
        EquipSlot.Ammo -> if (material == "metal") "equip_bolts" else "equip_range"
        EquipSlot.Ring -> if (name == "beacon ring") "equip_surok_ring" else "equip_clothes"
        else -> "equip_clothes"
    }
    if (name.contains("jester")) {
        sound = "equip_jester"
    }
    if (material == "leather") {
        sound = "equip_leather"
    }
    if (material == "wood") {
        sound = "equip_wood"
    }
    player.playSound(sound)
}