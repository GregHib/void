package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.combat.weaponSwing

fun isWarSpear(item: Item) = item.id.startsWith("guthans_warspear")

weaponSwing("guthans_warspear*", Priority.LOW) { player: Player ->
    player.setAnimation("guthans_spear_${
        when (player.attackType) {
            "swipe" -> "swipe"
            else -> "attack"
        }
    }")
    player.hit(target)
    delay = 5
}

combatAttack({ !blocked && target is Player && isWarSpear(target.weapon) }) { _: Character ->
    target.setAnimation("guthans_spear_block", delay)
    blocked = true
}