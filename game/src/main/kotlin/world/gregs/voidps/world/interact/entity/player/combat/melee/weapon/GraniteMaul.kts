package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon

fun isMaul(item: Item) = item.id.startsWith("granite_maul")

on<CombatSwing>({ !swung() && isMaul(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("granite_maul_attack")
    player.hit(target)
    delay = 7
}

on<CombatAttack>({ !blocked && target is Player && isMaul(target.weapon) }) { _: Character ->
    target.setAnimation("granite_maul_block", delay)
    blocked = true
}