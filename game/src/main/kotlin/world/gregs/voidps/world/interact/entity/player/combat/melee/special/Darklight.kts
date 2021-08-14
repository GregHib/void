package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import kotlin.math.floor

fun isDemon(target: Character?): Boolean = target != null

fun isDarklight(weapon: Item?) = weapon != null && weapon.name == "darklight"

on<HitDamageModifier>({ type == "melee" && isDarklight(weapon) && isDemon(target) }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.60)
}

on<CombatHit>({ isDarklight(weapon) && special }) { character: Character ->
    if (damage <= 0) {
        return@on
    }

    val amount = if (isDemon(character)) 0.10 else 0.05
    character.levels.drain(Skill.Attack, multiplier = amount)
    character.levels.drain(Skill.Strength, multiplier = amount)
    character.levels.drain(Skill.Defence, multiplier = amount)
}