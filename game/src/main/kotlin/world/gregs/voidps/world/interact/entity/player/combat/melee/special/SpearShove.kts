package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.move
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setForceMovement
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.toTicks
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import java.util.concurrent.TimeUnit

fun isDragonSpear(item: Item?) = item != null && (item.name.startsWith("dragon_spear") || item.name.startsWith("zamorakian_spear"))

on<CombatSwing>({ !swung() && it.specialAttack && isDragonSpear(it.weapon) }) { player: Player ->
    if (target.size.width > 1 || target.size.height > 1) {
        player.message("That creature is too large to knock back!")
        delay = -1
        return@on
    }
    if (target.hasEffect("stun_immunity")) {
        player.message("That ${if (target is Player) "player" else "creature"} is already stunned!")
        delay = -1
        return@on
    }
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 4)) {
        delay = -1
        return@on
    }
    player.setAnimation("shove")
    player.setGraphic("shove")
    val duration = TimeUnit.SECONDS.toTicks(3)
    target.setGraphic("shove_stun")
    target.start("stun", duration)
    player.hit(target, damage = -1)// Hit with no damage so target can auto-retaliate
    val actual = player.tile
    val direction = target.tile.delta(actual).toDirection()
    val delta = direction.delta
    if (!target.movement.traversal.blocked(target.tile, direction)) {
        target.setForceMovement(delta, 30, direction = direction.inverse())
        delay(1) {
            target.move(delta)
        }
    }
    delay = 4
}