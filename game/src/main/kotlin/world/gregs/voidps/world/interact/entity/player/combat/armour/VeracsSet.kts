package world.gregs.voidps.world.interact.entity.player.combat.armour

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.ItemChanged
import world.gregs.voidps.type.random
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.HitChanceModifier

on<Registered>({ it.hasFullSet() }) { player: Player ->
    player["veracs_set_effect"] = true
}

on<ItemChanged>({ inventory == "worn_equipment" && isSetSlot(index) && it.contains("veracs_set_effect") && !isVeracs(item) }) { player: Player ->
    player.clear("veracs_set_effect")
}

on<ItemChanged>({ inventory == "worn_equipment" && isSetSlot(index) && !it.contains("veracs_set_effect") && isVeracs(item) && it.hasFullSet() }) { player: Player ->
    player["veracs_set_effect"] = true
}

fun isVeracs(item: Item) = item.id.startsWith("veracs_")

fun isSetSlot(index: Int) = index == EquipSlot.Hat.index || index == EquipSlot.Chest.index || index == EquipSlot.Legs.index || index == EquipSlot.Weapon.index

fun Player.hasFullSet(): Boolean {
    return equipped(EquipSlot.Chest).id.startsWith("veracs_brassard") &&
            equipped(EquipSlot.Legs).id.startsWith("veracs_plateskirt") &&
            equipped(EquipSlot.Weapon).id.startsWith("veracs_flail") &&
            equipped(EquipSlot.Hat).id.startsWith("veracs_helm")
}

on<HitChanceModifier>({ type == "melee" && it.contains("veracs_set_effect") && random.nextInt(4) == 0 }, Priority.HIGHEST) { _: Character ->
    chance = 1.0
}