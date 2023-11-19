package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.combat.hit.HitChanceModifier
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.melee.drainByDamage
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

fun isBoneDagger(item: Item) = item.id.startsWith("bone_dagger")

on<HitChanceModifier>({ special && isBoneDagger(weapon) }, Priority.HIGHEST) { player: Player ->
    val last = target.attackers.lastOrNull() ?: return@on
    if (last != player) {
        chance = 1.0
    }
}

on<CombatSwing>({ !swung() && it.specialAttack && isBoneDagger(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, 750)) {
        delay = -1
        return@on
    }
    player.setAnimation("backstab")
    player.setGraphic("backstab")
    val damage = player.hit(target)
    target.drainByDamage(damage, Skill.Defence)
    delay = 4
}