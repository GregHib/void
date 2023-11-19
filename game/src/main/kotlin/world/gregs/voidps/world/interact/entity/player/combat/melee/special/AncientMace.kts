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
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

fun isAncientMace(item: Item?) = item != null && item.id == "ancient_mace"

on<CombatSwing>({ !swung() && it.specialAttack && isAncientMace(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK)) {
        delay = -1
        return@on
    }
    player.setAnimation("favour_of_the_war_god")
    player.setGraphic("favour_of_the_war_god")
    val damage = player.hit(target)
    if (damage != -1) {
        val drain = damage / 10
        if (drain > 0) {
            target.levels.drain(Skill.Prayer, drain)
            player.levels.restore(Skill.Prayer, drain)
        }
    }
    delay = 5
}