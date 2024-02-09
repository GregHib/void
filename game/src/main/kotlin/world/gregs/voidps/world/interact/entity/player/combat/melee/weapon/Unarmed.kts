package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.block
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.combat.weaponSwing

val definitions: WeaponStyleDefinitions by inject()

weaponSwing(priority = Priority.LOWEST) { player: Player ->
    val id = player.weapon.def["weapon_style", 0]
    val style = definitions.get(id)
    player.setAnimation("${style.stringId}_${player.attackType}")
    player.hit(target)
    delay = player.weapon.def["attack_speed", 4]
}

block(Priority.LOW) {
    if (target is Player) {
        val id = target.weapon.def["weapon_style", 0]
        val style = definitions.get(id)
        target.setAnimation("${style.stringId}_hit", delay)
        blocked = true
    }
}