package world.gregs.voidps.world.interact.entity.player.combat.armour.barrows

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.ItemChanged
import world.gregs.voidps.world.interact.entity.combat.hit.HitDamageModifier
import kotlin.math.floor

on<Registered>({ it.hasFullSet() }) { player: Player ->
    player["dharoks_set_effect"] = true
}

on<ItemChanged>({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && it.contains("dharoks_set_effect") && !isDharoks(item) }) { player: Player ->
    player.clear("dharoks_set_effect")
}

on<ItemChanged>({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && !it.contains("dharoks_set_effect") && isDharoks(item) && it.hasFullSet() }) { player: Player ->
    player["dharoks_set_effect"] = true
}

fun isDharoks(item: Item) = item.id.startsWith("dharoks_")

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "dharoks_greataxe",
    "dharoks_helm",
    "dharoks_platebody",
    "dharoks_platelegs")

on<HitDamageModifier>({ type == "melee" && weapon.id.startsWith("dharoks_greataxe") && it.contains("dharoks_set_effect") }, Priority.LOW) { player: Player ->
    val lost = (player.levels.getMax(Skill.Constitution) - player.levels.get(Skill.Constitution)) / 1000.0
    val max = player.levels.getMax(Skill.Constitution) / 1000.0
    damage = floor(damage * (1 + lost * max))
}