package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

fun isVestas(item: Item) = item.id.endsWith("vestas_longsword")

combatSwing({ !swung() && isVestas(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("vestas_longsword_${
        when (player.attackType) {
            "lunge" -> "lunge"
            else -> "attack"
        }
    }")
    player.hit(target)
    delay = 5
}

combatAttack({ !blocked && target is Player && isVestas(target.weapon) }) { _: Character ->
    target.setAnimation("vestas_longsword_block", delay)
    blocked = true
}

// Special attack

combatSwing({ !swung() && it.specialAttack && isVestas(it.weapon) }) { player: Player ->
    if (player.specialAttack && !drainSpecialEnergy(player, 250)) {
        delay = -1
        return@combatSwing
    }
    player.setAnimation("vestas_longsword_feint")
    player.hit(target)
    delay = 5
}
