package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.specialAttackSwing
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.proj.shoot

specialAttackSwing("zaniks_crossbow", style = "range", priority = Priority.HIGHISH) { player: Player ->
    if (!drainSpecialEnergy(player, 500)) {
        delay = -1
        return@specialAttackSwing
    }
    player.setAnimation("zaniks_crossbow_special")
    player.setGraphic("zaniks_crossbow_special")
    player.shoot(id = "zaniks_crossbow_bolt", target = target)
    val distance = player.tile.distanceTo(target)
    val damage = player.hit(target, delay = Hit.bowDelay(distance))
    if (damage != -1) {
        target.levels.drain(Skill.Defence, damage / 10)
    }
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
}