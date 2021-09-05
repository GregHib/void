package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.range.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.range.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.range.special.specialAttack

fun isDragonHatchet(item: Item?) = item != null && item.name == "dragon_hatchet"

on<CombatSwing>({ !swung() && it.specialAttack && isDragonHatchet(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK)) {
        delay = -1
        return@on
    }
    player.setAnimation("clobber")
    player.setGraphic("clobber")
    player.hit(target)
    delay = 5
}

on<CombatHit>({ weapon != null && special && isDragonHatchet(weapon) }) { character: Character ->
    val drain = damage / 100
    if (drain > 0) {
        character.levels.drain(Skill.Defence, drain)
        character.levels.drain(Skill.Magic, drain)
    }
}