package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.forceWalk
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.character.size
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.effect.freeze
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import java.util.concurrent.TimeUnit

fun isDragonSpear(item: Item) = item.id.startsWith("dragon_spear") || item.id.startsWith("zamorakian_spear")

combatSwing({ !swung() && it.specialAttack && isDragonSpear(it.weapon) }) { player: Player ->
    if (target.size > 1) {
        player.message("That creature is too large to knock back!")
        delay = -1
        return@combatSwing
    }
    if (target.hasClock("movement_delay")) {
        player.message("That ${if (target is Player) "player" else "creature"} is already stunned!")
        delay = -1
        return@combatSwing
    }
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 4)) {
        delay = -1
        return@combatSwing
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
        target.forceWalk(delta, 30, direction.inverse())
    }
    delay = 4
}