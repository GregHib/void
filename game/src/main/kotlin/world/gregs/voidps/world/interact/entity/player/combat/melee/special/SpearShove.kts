package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.size
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.entity.combat.combatPrepare
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.effect.freeze
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import java.util.concurrent.TimeUnit

combatPrepare("melee") { player ->
    if (!player.specialAttack || player.weapon.def["special", ""] != "shove") {
        return@combatPrepare
    }
    if (target.size > 1) {
        player.message("That creature is too large to knock back!")
        cancel()
    } else if (target.hasClock("movement_delay")) {
        player.message("That ${if (target is Player) "player" else "creature"} is already stunned!")
        cancel()
    }
}

specialAttack("shove") { player ->
    player.setAnimation("${id}_special")
    player.gfx("${id}_special")
    val duration = TimeUnit.SECONDS.toTicks(3)
    target.gfx("shove_hit")
    target.freeze(duration)
    player["delay"] = duration
    player.hit(target, damage = -1) // Hit with no damage so target can auto-retaliate
    val actual = player.tile
    val direction = target.tile.delta(actual).toDirection()
    val delta = direction.delta
    if (!target.blocked(direction)) {
        target.exactMove(delta, 30, direction.inverse())
    }
}