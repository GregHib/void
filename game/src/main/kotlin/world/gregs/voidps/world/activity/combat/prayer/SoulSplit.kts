import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatDamage
import world.gregs.voidps.world.interact.entity.proj.shoot

on<CombatDamage>({ it.hasEffect("prayer_soul_split") && damage > 0 }) { player: Player ->
    val closeCombat = player.tile.distanceTo(target) <= 1
    player.shoot("soul_split", target, height = 10, endHeight = 10, flightTime = if (closeCombat) 40 else 80)
    delay(target, if (closeCombat) 1 else 2) {
        target.setGraphic("soul_split_hit")
        target.shoot("soul_split", player, height = 10, endHeight = 10, flightTime = if (closeCombat) 40 else 80)
    }
}