package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon

fun isFlail(item: Item?) = item != null && item.id.startsWith("veracs_flail")

on<CombatSwing>({ !swung() && isFlail(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("veracs_flail_attack")
    player.hit(target)
    delay = 5
}

on<CombatAttack>({ !blocked && target is Player && isFlail(target.weapon) }) { _: Character ->
    target.setAnimation("veracs_flail_block", delay)
    blocked = true
}