import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatAttack

on<CombatAttack>({ !blocked }, Priority.LOWER) { _: Player ->
    target.setAnimation("block", delay)
    blocked = true
}