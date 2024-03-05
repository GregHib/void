package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.exactMove
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.character.size
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.effect.freeze
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import java.util.concurrent.TimeUnit

val handler: suspend CombatSwing.(Player) -> Unit = handler@{ player ->
    if (target.size > 1) {
        player.message("That creature is too large to knock back!")
        delay = -1
        return@handler
    }
    if (target.hasClock("movement_delay")) {
        player.message("That ${if (target is Player) "player" else "creature"} is already stunned!")
        delay = -1
        return@handler
    }
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 4)) {
        delay = -1
        return@handler
    }
    player.setAnimation("shove")
    player.setGraphic("shove")
    val duration = TimeUnit.SECONDS.toTicks(3)
    target.setGraphic("shove_stun")
    target.freeze(duration)
    player.start("delay", duration)
    player.hit(target, damage = -1)// Hit with no damage so target can auto-retaliate
    val actual = player.tile
    val direction = target.tile.delta(actual).toDirection()
    val delta = direction.delta
    if (!target.blocked(direction)) {
        target.exactMove(delta, 30, direction.inverse())
    }
    delay = 4
}
combatSwing("dragon_spear", "melee", special = true, block = handler)
combatSwing("zamorakian_spear", "melee", special = true, block = handler)