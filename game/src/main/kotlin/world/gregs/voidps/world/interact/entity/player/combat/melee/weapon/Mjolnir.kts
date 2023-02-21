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

fun isMjolnir(item: Item?) = item != null && item.id.endsWith("mjolnir")

on<CombatSwing>({ !swung() && isMjolnir(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("mjolnir_attack")
    player.hit(target)
    delay = 6
}

on<CombatAttack>({ !blocked && target is Player && isMjolnir(target.weapon) }, Priority.LOW) { _: Character ->
    target.setAnimation("mjolnir_hit", delay)
    blocked = true
}