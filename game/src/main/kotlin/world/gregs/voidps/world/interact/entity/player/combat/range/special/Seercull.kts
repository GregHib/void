package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.levelChange
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.combatHit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.specialAttackSwing
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

fun isSeercull(weapon: Item) = weapon.id == "seercull"

specialAttackSwing("seercull", style = "range", priority = Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK)) {
        delay = -1
        return@specialAttackSwing
    }
    player.setAnimation("bow_accurate")
    player.setGraphic("seercull_special_shoot")
    player.playSound("seercull_special")
    player.shoot(id = "seercull_special_arrow", target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = Hit.bowDelay(distance))
}

combatHit({ source is Player && isSeercull(weapon) && special }) { character: Character ->
    character.setGraphic("seercull_special_hit")
}

combatAttack { _: Player ->
    if (!isSeercull(weapon) || !special || target["soulshot", false]) {
        return@combatAttack
    }
    target["soulshot"] = true
    target.levels.drain(Skill.Magic, damage / 10)
}

levelChange({ skill == Skill.Magic && it["soulshot", false] && to >= it.levels.getMax(skill) }) { character: Character ->
    character.clear("soulshot")
}