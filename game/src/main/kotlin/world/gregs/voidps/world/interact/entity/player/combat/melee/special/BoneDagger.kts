package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.range.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.range.special.specialAttack

fun isBoneDagger(item: Item?) = item != null && item.name.startsWith("bone_dagger")

on<HitChanceModifier>({ special && isBoneDagger(weapon) }, Priority.HIGHEST) { _: Player ->
    // TODO if not the latest attacker
    chance = 1.0
}

on<CombatSwing>({ !swung() && it.specialAttack && isBoneDagger(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, 750)) {
        delay = -1
        return@on
    }
    player.setAnimation("backstab")
    player.setGraphic("backstab", height = 100)
    player.hit(target)
    delay = 4
}

on<CombatHit>({ isBoneDagger(weapon) && special }) { character: Character ->
    val drain = damage / 10
    if (drain > 0) {
        character.levels.drain(Skill.Defence, drain)
    }
}