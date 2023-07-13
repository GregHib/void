package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.CurrentLevelChanged
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.bowHitDelay
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

fun isSeercull(weapon: Item?) = weapon != null && weapon.id == "seercull"

on<HitChanceModifier>({ type == "range" && special && isSeercull(weapon) }, Priority.HIGHEST) { _: Player ->
    chance = 1.0
}

on<CombatSwing>({ player -> !swung() && player.fightStyle == "range" && player.specialAttack && isSeercull(player.weapon) }, Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK)) {
        delay = -1
        return@on
    }
    player.setAnimation("bow_shoot")
    player.setGraphic("seercull_special_shoot")
    player.playSound("seercull_special")
    player.shoot(id = "seercull_special_arrow", target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = bowHitDelay(distance))
}

on<CombatHit>({ source is Player && isSeercull(weapon) && special }) { character: Character ->
    character.setGraphic("seercull_special_hit")
    if (!character["soulshot", false]) {
        character["soulshot"] = true
        character.levels.drain(Skill.Magic, damage / 10)
    }
}

on<CurrentLevelChanged>({ skill == Skill.Magic && it["soulshot", false] && to >= it.levels.getMax(skill) }) { character: Character ->
    character.clear("soulshot")
}