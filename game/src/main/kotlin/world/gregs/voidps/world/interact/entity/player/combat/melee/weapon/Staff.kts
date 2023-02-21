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

fun isStaff(item: Item?) = item != null && (item.def["category", ""] == "staff" || item.id.endsWith("wand") || item.id.endsWith("crozier"))

on<CombatSwing>({ !swung() && isStaff(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("staff_attack")
    player.hit(target)
    delay = 4
}

on<CombatAttack>({ !blocked && target is Player && isStaff(target.weapon) }, Priority.LOW) { _: Character ->
    target.setAnimation("staff_block", delay)
    blocked = true
}