package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.melee.drainByDamage
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack

fun isBoneDagger(item: Item?) = item != null && item.id.startsWith("bone_dagger")

on<HitChanceModifier>({ special && isBoneDagger(weapon) }, Priority.HIGHEST) { player: Player ->
    val last = target?.attackers?.lastOrNull() ?: return@on
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