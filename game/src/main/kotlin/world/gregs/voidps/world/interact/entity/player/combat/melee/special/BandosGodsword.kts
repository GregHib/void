package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialAccuracyMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialDamageMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialDrainByDamage
import world.gregs.voidps.world.interact.entity.player.combat.range.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.range.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.range.special.specialAttack
import kotlin.math.floor

fun isBandosGodsword(weapon: Item?) = weapon != null && weapon.name.startsWith("bandos_godsword")

on<HitDamageModifier>({ type == "melee" && special && isBandosGodsword(weapon) }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.1)
}

specialDamageMultiplier(1.1, ::isBandosGodsword)
specialAccuracyMultiplier(2.0, ::isBandosGodsword)
specialDrainByDamage(::isBandosGodsword, Skill.Defence, Skill.Strength, Skill.Prayer, Skill.Attack, Skill.Magic, Skill.Range)

on<CombatSwing>({ !swung() && it.specialAttack && isBandosGodsword(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    player.setAnimation("warstrike")
    player.setGraphic("warstrike")
    player.hit(target)
    delay = 6
}
