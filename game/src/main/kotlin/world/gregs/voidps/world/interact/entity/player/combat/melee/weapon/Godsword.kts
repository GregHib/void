package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon

fun isGodsword(item: Item) = item.id.endsWith("godsword") || item.id == "saradomin_sword"

on<CombatSwing>({ !swung() && isGodsword(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("godsword_${player.attackType}")
    player.hit(target)
    delay = 6
}

on<CombatAttack>({ !blocked && target is Player && isGodsword(target.weapon) }, Priority.LOW) { _: Character ->
    target.setAnimation("godsword_hit", delay)
    blocked = true
}