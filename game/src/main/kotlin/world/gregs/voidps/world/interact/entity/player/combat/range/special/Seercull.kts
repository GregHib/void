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
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.fightStyle
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.CombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

fun isSeercull(weapon: Item) = weapon.id == "seercull"

on<CombatSwing>({ player -> !swung() && player.fightStyle == "range" && player.specialAttack && isSeercull(player.weapon) }, Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK)) {
        delay = -1
        return@on
    }
    player.setAnimation("bow_accurate")
    player.setGraphic("seercull_special_shoot")
    player.playSound("seercull_special")
    player.shoot(id = "seercull_special_arrow", target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = Hit.bowDelay(distance))
}

on<CombatHit>({ source is Player && isSeercull(weapon) && special }) { character: Character ->
    character.setGraphic("seercull_special_hit")
}

on<CombatAttack>({ isSeercull(weapon) && special }) { _: Player ->
    if (!target["soulshot", false]) {
        target["soulshot"] = true
        target.levels.drain(Skill.Magic, damage / 10)
    }
}

on<CurrentLevelChanged>({ skill == Skill.Magic && it["soulshot", false] && to >= it.levels.getMax(skill) }) { character: Character ->
    character.clear("soulshot")
}