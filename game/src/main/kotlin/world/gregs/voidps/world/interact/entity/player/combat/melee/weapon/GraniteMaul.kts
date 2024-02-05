package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon

fun isMaul(item: Item) = item.id.startsWith("granite_maul")

combatSwing({ !swung() && isMaul(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("granite_maul_attack")
    player.hit(target)
    delay = 7
}

combatAttack({ !blocked && target is Player && isMaul(target.weapon) }) { _: Character ->
    target.setAnimation("granite_maul_block", delay)
    blocked = true
}