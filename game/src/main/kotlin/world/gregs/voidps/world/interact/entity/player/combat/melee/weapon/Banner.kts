package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon

fun isBanner(item: Item?) = item != null && (item.id.startsWith("banner") || item.id.startsWith("rat_pole") || item.id.endsWith("flag"))

on<CombatSwing>({ !swung() && isBanner(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("banner_attack")
    player.hit(target)
    delay = 4
}

on<CombatAttack>({ !blocked && target is Player && isBanner(target.weapon) }, Priority.LOW) { _: Character ->
    target.setAnimation("banner_hit", delay)
    blocked = true
}