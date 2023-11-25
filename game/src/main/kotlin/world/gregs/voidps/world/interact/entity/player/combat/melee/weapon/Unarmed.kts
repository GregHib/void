package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon

val definitions: WeaponStyleDefinitions by inject()

on<CombatSwing>({ !swung() }, Priority.LOWEST) { player: Player ->
    val id = player.weapon.def["weapon_style", 0]
    val style = definitions.get(id)
    player.setAnimation("${style.stringId}_${player.attackType}")
    player.hit(target)
    delay = 4//style.attackSpeed
}

on<CombatAttack>({ !blocked && target is Player }, Priority.LOW) { _: Character ->
    val id = target.weapon.def["weapon_style", 0]
    val style = definitions.get(id)
    target.setAnimation("${style.stringId}_hit", delay)
    blocked = true
}