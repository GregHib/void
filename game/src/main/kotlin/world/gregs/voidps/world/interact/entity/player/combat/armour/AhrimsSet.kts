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
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.CombatAttack
import kotlin.random.Random

on<Registered>({ it.hasFullSet() }) { player: Player ->
    player["ahrims_set_effect"] = true
}

on<ItemChanged>({ inventory == "worn_equipment" && isSetSlot(index) && it.contains("ahrims_set_effect") && !isAhrims(item) }) { player: Player ->
    player.clear("ahrims_set_effect")
}

on<ItemChanged>({ inventory == "worn_equipment" && isSetSlot(index) && !it.contains("ahrims_set_effect") && isAhrims(item) && it.hasFullSet() }) { player: Player ->
    player["ahrims_set_effect"] = true
}

fun isAhrims(item: Item) = item.id.startsWith("ahrims_")

fun isSetSlot(index: Int) = index == EquipSlot.Hat.index || index == EquipSlot.Chest.index || index == EquipSlot.Legs.index || index == EquipSlot.Weapon.index

fun Player.hasFullSet(): Boolean {
    return equipped(EquipSlot.Chest).id.startsWith("ahrims_robe_top") &&
            equipped(EquipSlot.Legs).id.startsWith("ahrims_robe_skirt") &&
            equipped(EquipSlot.Weapon).id.startsWith("ahrims_staff") &&
            equipped(EquipSlot.Hat).id.startsWith("ahrims_hood")
}

on<CombatAttack>({ type == "magic" && it.contains("ahrims_set_effect") && damage > 0 && Random.nextInt(4) == 0 }) { _: Character ->
    if (target.levels.drain(Skill.Strength, 5) < 0) {
        target.setGraphic("ahrims_effect")
    }
}