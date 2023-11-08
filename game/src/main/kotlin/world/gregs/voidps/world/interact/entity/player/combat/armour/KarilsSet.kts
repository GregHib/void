package world.gregs.voidps.world.interact.entity.player.combat.armour

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.ItemChanged
import world.gregs.voidps.type.random
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.CombatAttack

on<Registered>({ it.hasFullSet() }) { player: Player ->
    player["karils_set_effect"] = true
}

on<ItemChanged>({ inventory == "worn_equipment" && isSetSlot(index) && it.contains("karils_set_effect") && !isKarils(item) }) { player: Player ->
    player.clear("karils_set_effect")
}

on<ItemChanged>({ inventory == "worn_equipment" && isSetSlot(index) && !it.contains("karils_set_effect") && isKarils(item) && it.hasFullSet() }) { player: Player ->
    player["karils_set_effect"] = true
}

fun isKarils(item: Item) = item.id.startsWith("karils_")

fun isSetSlot(index: Int) = index == EquipSlot.Hat.index || index == EquipSlot.Chest.index || index == EquipSlot.Legs.index || index == EquipSlot.Weapon.index

fun Player.hasFullSet(): Boolean {
    return equipped(EquipSlot.Chest).id.startsWith("karils_top") &&
            equipped(EquipSlot.Legs).id.startsWith("karils_skirt") &&
            equipped(EquipSlot.Weapon).id.startsWith("karils_crossbow") &&
            equipped(EquipSlot.Hat).id.startsWith("karils_coif")
}

on<CombatAttack>({ type == "range" && damage > 0 && target is Player && weapon?.id?.startsWith("karils_crossbow") == true && it.contains("karils_set_effect") && random.nextInt(4) == 0 }) { _: Character ->
    if (target.levels.drain(Skill.Agility, multiplier = 0.20) < 0) {
        target.setGraphic("karils_effect")
    }
}