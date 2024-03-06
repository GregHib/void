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
import world.gregs.voidps.world.interact.entity.combat.combatPrepare
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.effect.freeze
import java.util.concurrent.TimeUnit

combatPrepare("melee") { player ->
    if (target.size > 1) {
        player.message("That creature is too large to knock back!")
        cancel()
    } else if (target.hasClock("movement_delay")) {
        player.message("That ${if (target is Player) "player" else "creature"} is already stunned!")
        cancel()
    }
}

val handler: suspend CombatSwing.(Player) -> Unit = handler@{ player ->
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