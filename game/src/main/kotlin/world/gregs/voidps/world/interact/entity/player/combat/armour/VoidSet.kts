package world.gregs.voidps.world.interact.entity.player.combat.armour

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.inv.ItemChanged
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.HitEffectiveLevelModifier
import kotlin.math.floor

on<Registered>({ it.hasFullSet("") }) { player: Player ->
    player["void_set_effect"] = true
}

on<Registered>({ it.hasFullSet("elite_") }) { player: Player ->
    player["elite_void_set_effect"] = true
}

on<ItemChanged>({ inventory == "worn_equipment" && isSetSlot(index) && it.contains("void_set_effect") && !isVoid(item) }) { player: Player ->
    player.clear("void_set_effect")
}

on<ItemChanged>({ inventory == "worn_equipment" && isSetSlot(index) && !it.contains("void_set_effect") && isVoid(item) && it.hasFullSet("") }) { player: Player ->
    player["void_set_effect"] = true
}

on<ItemChanged>({ inventory == "worn_equipment" && isSetSlot(index) && it.contains("elite_void_set_effect") && !isEliteVoid(item) }) { player: Player ->
    player.clear("elite_void_set_effect")
}

on<ItemChanged>({ inventory == "worn_equipment" && isSetSlot(index) && !it.contains("elite_void_set_effect") && isEliteVoid(item) && it.hasFullSet("elite_") }) { player: Player ->
    player["elite_void_set_effect"] = true
}

fun isVoid(item: Item) = item.id.startsWith("void_")

fun isEliteVoid(item: Item) = item.id.startsWith("elite_void_") || item.id == "void_knight_gloves" || isHelm(item)

fun isSetSlot(index: Int) = index == EquipSlot.Hat.index || index == EquipSlot.Chest.index || index == EquipSlot.Legs.index || index == EquipSlot.Hands.index

fun Player.hasFullSet(prefix: String): Boolean {
    return equipped(EquipSlot.Chest).id.startsWith("${prefix}void_knight_top") &&
            equipped(EquipSlot.Legs).id.startsWith("${prefix}void_knight_robe") &&
            equipped(EquipSlot.Hands).id.startsWith("void_knight_gloves") &&
            isHelm(equipped(EquipSlot.Hat))
}

fun isHelm(item: Item): Boolean = when (item.id) {
    "void_ranger_helm", "void_melee_helm", "void_mage_helm" -> true
    else -> false
}

on<HitEffectiveLevelModifier>({ (it.contains("void_set_effect") || it.contains("elite_void_set_effect")) && skill == Skill.Magic }, priority = Priority.HIGH) { _: Player ->
    level = floor(level * 1.45)
}

on<HitEffectiveLevelModifier>({ (it.contains("void_set_effect") || it.contains("elite_void_set_effect")) && skill != Skill.Magic }, priority = Priority.LOW) { _: Player ->
    level = floor(level * 1.1)
}