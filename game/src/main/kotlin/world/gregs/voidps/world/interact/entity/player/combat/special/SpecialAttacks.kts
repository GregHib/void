package world.gregs.voidps.world.interact.entity.player.combat.special

import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon

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
    player.setAnimation("${id}_special")
    player.setGraphic(id)
    val damage = player.hit(target)
    if (damage >= 0) {
        target.setAnimation("${id}_hit")
        target.setGraphic("${id}_hit")
        player.emit(SpecialAttackHit(id, target, damage))
    }
}

