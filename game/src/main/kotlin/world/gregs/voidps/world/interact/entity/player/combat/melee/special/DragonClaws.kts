package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import kotlin.random.Random.Default.nextBoolean
import kotlin.random.Random.Default.nextDouble
import kotlin.random.Random.Default.nextInt

fun isDragonClaws(weapon: Item?) = weapon != null && weapon.id == "dragon_claws"

on<CombatSwing>({ !swung() && it.specialAttack && isDragonClaws(it.weapon) }, Priority.LOW) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    player.setAnimation("slice_and_dice")
    player.setGraphic("slice_and_dice")

    val weapon = player.weapon
    var (hit1, hit2, hit3, hit4) = intArrayOf(0, 0, 0, 0)
    val maxHit = getMaximumHit(player, target, "melee", weapon)
    if (successfulHit(player, target, "melee", weapon, special = true)) {
        hit1 = nextInt(maxHit / 2, maxHit - 10)
        hit2 = hit1 / 2
        hit3 = hit2 / 2
        hit4 = hit3 + if (nextBoolean()) 10 else 0
    } else if (successfulHit(player, target, "melee", weapon, special = true)) {
        hit2 = nextDouble(maxHit * 0.375, maxHit * 0.875).toInt()
        hit3 = hit2 / 2
        hit4 = hit3 + if (nextBoolean()) 10 else 0
    } else if (successfulHit(player, target, "melee", weapon, special = true)) {
        hit3 = nextDouble(maxHit * 0.25, maxHit * 0.75).toInt()
        hit4 = hit3 + if (nextBoolean()) 10 else 0
    } else if (successfulHit(player, target, "melee", weapon, special = true)) {
        hit4 = nextDouble(maxHit * 0.25, maxHit * 1.25).toInt()
    } else {
        hit3 = if (nextBoolean()) 10 else 0
        hit4 = if (nextBoolean()) 10 else 0
    }

    player.hit(target, damage = hit1)
    player.hit(target, damage = hit2)
    player.hit(target, damage = hit3, delay = 1)
    player.hit(target, damage = hit4, delay = 1)
    delay = 4
}