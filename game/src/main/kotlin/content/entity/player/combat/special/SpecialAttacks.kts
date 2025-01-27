package content.entity.player.combat.special

import world.gregs.voidps.engine.client.variable.variableSet
import content.entity.combat.hit.hit
import content.skill.melee.weapon.weapon
import content.entity.sound.playSound

specialAttackPrepare("*") { player ->
    if (!SpecialAttack.hasEnergy(player)) {
        cancel()
    }
}

variableSet("special_attack", to = true) { player ->
    if (from == true) {
        return@variableSet
    }
    val id: String = player.weapon.def.getOrNull("special") ?: return@variableSet
    val prepare = SpecialAttackPrepare(id)
    player.emit(prepare)
    if (prepare.cancelled) {
        player.specialAttack = false
    }
}

specialAttack { player ->
    player.anim("${id}_special")
    player.gfx("${id}_special")
    player.playSound("${id}_special")
    val damage = player.hit(target)
    if (damage >= 0) {
        target.gfx("${id}_impact")
    }
    player.emit(SpecialAttackHit(id, target, damage))
}
