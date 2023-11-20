package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialAccuracyMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import kotlin.math.max

fun isSaradominGodsword(weapon: Item?) = weapon != null && weapon.id.startsWith("saradomin_godsword")

specialAccuracyMultiplier(2.0, ::isSaradominGodsword)

on<CombatSwing>({ !swung() && it.specialAttack && isSaradominGodsword(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    player.setAnimation("healing_blade")
    player.setGraphic("healing_blade")
    val damage = player.hit(target)
    if (damage != -1) {
        player.levels.restore(Skill.Constitution, max(100, damage / 20))
        player.levels.restore(Skill.Prayer, max(50, damage / 40))
    }
    delay = 6
}