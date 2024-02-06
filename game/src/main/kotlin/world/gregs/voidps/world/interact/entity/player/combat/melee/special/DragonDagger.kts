package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.specialAttackSwing
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.combat.weaponSwing
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy

fun isDragonDagger(item: Item) = item.id.startsWith("dragon_dagger") || item.id.startsWith("corrupt_dragon_dagger")

weaponSwing("*dragon_dagger*", Priority.LOW) { player: Player ->
    player.setAnimation("dragon_dagger_${
        when (player.attackType) {
            "slash" -> "slash"
            else -> "attack"
        }
    }")
    player.hit(target)
    delay = 4
}

combatAttack({ !blocked && target is Player && isDragonDagger(target.weapon) }) { _: Character ->
    target.setAnimation("dragon_dagger_block", delay)
    blocked = true
}

specialAttackSwing("dragon_dagger*", "corrupt_dragon_dagger*") { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 4)) {
        delay = -1
        return@specialAttackSwing
    }
    player.setAnimation("puncture")
    player.setGraphic("puncture")
    player.hit(target)
    player.hit(target)
    delay = 4
}