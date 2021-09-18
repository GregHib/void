package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialAccuracyMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialDamageMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialDrainByDamage
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack

fun isAnchor(item: Item?) = item != null && item.name == "barrelchest_anchor"

on<CombatSwing>({ !swung() && isAnchor(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("anchor_attack")
    player.hit(target)
    delay = 6
}

on<CombatHit>({ !blocked && isAnchor(it.weapon) }) { player: Player ->
    player.setAnimation("anchor_block")
    blocked = true
}

// Special attack

specialAccuracyMultiplier(2.0, ::isAnchor)
specialDamageMultiplier(1.1, ::isAnchor)
specialDrainByDamage(::isAnchor, Skill.Defence, Skill.Strength, Skill.Prayer, Skill.Attack, Skill.Magic, Skill.Range)

on<CombatSwing>({ !swung() && it.specialAttack && isAnchor(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    player.setAnimation("sunder")
    player.setGraphic("sunder")
    player.hit(target)
    delay = 6
}