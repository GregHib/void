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
    player["guthans_set_effect"] = true
}

on<ItemChanged>({ inventory == "worn_equipment" && isSetSlot(index) && it.contains("guthans_set_effect") && !isGuthans(item) }) { player: Player ->
    player.clear("guthans_set_effect")
}

on<ItemChanged>({ inventory == "worn_equipment" && isSetSlot(index) && !it.contains("guthans_set_effect") && isGuthans(item) && it.hasFullSet() }) { player: Player ->
    player["guthans_set_effect"] = true
}

fun isGuthans(item: Item) = item.id.startsWith("guthans_")

fun isSetSlot(index: Int) = index == EquipSlot.Hat.index || index == EquipSlot.Chest.index || index == EquipSlot.Legs.index || index == EquipSlot.Weapon.index

fun Player.hasFullSet(): Boolean {
    return equipped(EquipSlot.Chest).id.startsWith("guthans_platebody") &&
            equipped(EquipSlot.Legs).id.startsWith("guthans_chainskirt") &&
            equipped(EquipSlot.Weapon).id.startsWith("guthans_warspear") &&
            equipped(EquipSlot.Hat).id.startsWith("guthans_helm")
}

on<CombatAttack>({ type == "melee" && it.contains("guthans_set_effect") && Random.nextInt(4) == 0 }) { character: Character ->
    character.levels.boost(Skill.Constitution, damage)
    target.setGraphic("guthans_effect")
}