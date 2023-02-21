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

fun isGoldenHammer(item: Item?) = item != null && item.id == "golden_hammer"

on<CombatSwing>({ !swung() && isGoldenHammer(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("tzhaar_ket_om_attack")
    player.hit(target)
    delay = 6
}

on<CombatAttack>({ !blocked && target is Player && isGoldenHammer(target.weapon) }) { _: Character ->
    target.setAnimation("tzhaar_ket_om_block", delay)
    blocked = true
}