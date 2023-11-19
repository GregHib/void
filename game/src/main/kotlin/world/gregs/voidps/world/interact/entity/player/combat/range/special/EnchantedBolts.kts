package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.splat
import world.gregs.voidps.world.interact.entity.player.effect.freeze
import world.gregs.voidps.world.interact.entity.player.toxin.poison
import java.util.concurrent.TimeUnit
import kotlin.math.floor

on<HitDamageModifier>({ target != null && type == "range" && target.hasClock("lucky_lightning") }, Priority.LOW) { player: Player ->
    damage += floor(player.levels.get(Skill.Ranged) * 0.1)
}

on<HitDamageModifier>({ target != null && type == "range" && target.hasClock("armour_piercing") }, Priority.HIGH) { _: Player ->
    damage = floor(damage * 1.15)
}

// TODO other staves and npcs
fun isFirey(target: Character?): Boolean = target is Player && target.equipped(EquipSlot.Weapon).id == "staff_of_fire"
fun isWatery(target: Character?): Boolean = target is Player && target.equipped(EquipSlot.Weapon).id == "staff_of_water"

on<HitDamageModifier>({ target != null && type == "range" && target.hasClock("sea_curse") && !isWatery(target) }, Priority.LOW) { player: Player ->
    damage += floor(player.levels.get(Skill.Ranged) * if (isFirey(target)) 1.0 / 15.0 else 0.05)
}

on<CombatAttack>({ char -> type == "range" && char.hasClock("dragons_breath") && !isFirey(char) }) { player: Player ->
    splat(target, player, player.levels.get(Skill.Ranged) * 2, "dragonfire", weapon)
}

on<HitDamageModifier>({ target != null && type == "range" && target.hasClock("blood_forfeit") }, Priority.LOW) { player: Player ->
    damage = floor(player.levels.get(Skill.Constitution) * 0.2)
}

fun isUndead(target: Character?): Boolean = target != null

on<HitDamageModifier>({ target != null && type == "range" && target.hasClock("life_leech") && !isUndead(target) }, Priority.HIGH) { _: Player ->
    damage = floor(damage * 1.20)
}

on<CombatAttack>({ char -> type == "range" && char.hasClock("life_leech") && damage >= 4 }) { player: Player ->
    player.levels.restore(Skill.Constitution, damage / 4)
}

on<CombatAttack>({ char -> type == "range" && char.hasClock("earths_fury") }) { player: Player ->
    val duration = TimeUnit.SECONDS.toTicks(5)
    target.freeze(duration)
    player.start("delay", duration)
}

on<CombatAttack>({ char -> type == "range" && char.hasClock("down_to_earth") }) { _: Player ->
    target.levels.drain(Skill.Magic, 1)
}

on<CombatAttack>({ char -> type == "range" && char.hasClock("clear_mind") }) { player: Player ->
    val amount = floor(player["range", 0] * 0.05).toInt()
    target.levels.drain(Skill.Prayer, amount)
    player.levels.restore(Skill.Prayer, amount / 2)
}

on<CombatAttack>({ char -> type == "range" && char.hasClock("magical_poison") }) { player: Player ->
    player.poison(target, 50)
}

on<CombatAttack>({ char -> type == "range" && char.hasClock("blood_forfeit") }) { player: Player ->
    val drain = floor(player.levels.get(Skill.Constitution) * 0.1).toInt()
    player.levels.drain(Skill.Constitution, drain)
}