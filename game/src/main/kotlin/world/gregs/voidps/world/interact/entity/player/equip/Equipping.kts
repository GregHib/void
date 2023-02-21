package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.contain.*
import world.gregs.voidps.engine.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.emote
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.equip.has
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.item.hasRequirements
import world.gregs.voidps.engine.entity.item.slot
import world.gregs.voidps.engine.entity.item.type
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.sound.playSound

fun canWear(option: String) = option == "Wield" || option == "Wear" || option == "Hold" || option == "Equip"

on<ContainerOption>({ container == "inventory" && canWear(option) }) { player: Player ->
    val def = item.def

    if (!player.hasRequirements(def, true)) {
        return@on
    }
    if (replaceWeaponShieldWith2h(player, def) && !player.equipment.move(EquipSlot.Shield.index, player.inventory)) {
        player.inventoryFull()
        return@on
    }

    if (replace2hWithShield(player, def) || replaceShieldWith2h(player, def)) {
        player.inventory.move(slot, player.equipment, item.slot.index)
        player.equipment.move(getOtherHandSlot(item.slot).index, player.inventory)
    } else {
        val target = player.equipment[item.slot.index]
        if (item.id == target.id && player.equipment.stackable(target.id)) {
            player.inventory.move(slot, player.equipment, item.slot.index)
        } else {
            player.inventory.swap(slot, player.equipment, item.slot.index)
        }
    }
    player.flagAppearance()
    playEquipSound(player, def)
}

on<ContainerOption>({ container == "worn_equipment" && option == "Remove" }) { player: Player ->
    player.equipment.move(slot, player.inventory)
    when (player.equipment.transaction.error) {
        TransactionError.None -> playEquipSound(player, item.def)
        is TransactionError.Full -> player.inventoryFull()
        else -> {}
    }
}

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index }) { player: Player ->
    updateWeaponEmote(player)
}

on<Registered> { player: Player ->
    updateWeaponEmote(player)
}

fun replaceWeaponShieldWith2h(player: Player, item: ItemDefinition) =
    player.has(EquipSlot.Shield) && player.has(EquipSlot.Weapon) && item.type == EquipType.TwoHanded

fun replaceShieldWith2h(player: Player, item: ItemDefinition) =
    player.has(EquipSlot.Shield) && !player.has(EquipSlot.Weapon) && item.type == EquipType.TwoHanded

fun replace2hWithShield(player: Player, item: ItemDefinition) =
    player.equipped(EquipSlot.Weapon).type == EquipType.TwoHanded && item.slot == EquipSlot.Shield

fun getOtherHandSlot(slot: EquipSlot) = if (slot == EquipSlot.Shield) EquipSlot.Weapon else EquipSlot.Shield

fun updateWeaponEmote(player: Player) {
    val weapon = player.equipped(EquipSlot.Weapon)
    val anim = weapon.def.getParam(644, 1426)
    player.emote = anim
    player.flagAppearance()
}

fun playEquipSound(player: Player, item: ItemDefinition) {
    val name = item.name.lowercase()
    val material = item["material", "cloth"]
    var sound = when (item.slot) {
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
    player.queue.clearWeak()
}