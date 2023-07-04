package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.world.activity.combat.prayer.praying
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.player.combat.magicHitDelay
import world.gregs.voidps.world.interact.entity.proj.shoot
import kotlin.math.sign

val definitions: SpellDefinitions by inject()

on<CombatSwing>({ player -> !swung() && player.spell == "teleport_block" }, Priority.LOW) { player: Player ->
    if (target is NPC) {
        delay = -1
        player.message("You can't use that against an NPC.")
        return@on
    }
    val spell = player.spell
    player.setAnimation("${spell}_cast")
    player.setGraphic("${spell}_cast")
    player.shoot(id = player.spell, target = target)
    val distance = player.tile.distanceTo(target)
    if (player.hit(target, delay = magicHitDelay(distance)) != -1) {
        val duration: Int = definitions.get(player.spell)["block_ticks"]
        player.teleBlock(target, duration)
    }
    delay = 5
}

on<TimerStart>({ timer == "teleport_block" }) { player: Player ->
    if (!restart) {
        player.message("You have been teleblocked.")
    }
    interval = 50
}

on<TimerStart>({ timer == "teleport_block" && it.teleBlockImmune }, Priority.HIGH) { _: Player ->
    cancel()
}

fun Player.protectMagic() = praying("deflect_magic") || praying("protect_from_magic")

on<TimerStart>({ timer == "teleport_block" && it.protectMagic() }, Priority.HIGH) { player: Player ->
    player.teleBlockCounter /= 2
}

on<TimerTick>({ timer == "teleport_block" }) { player: Player ->
    val blocked = player.teleBlocked
    player.teleBlockCounter -= player.teleBlockCounter.sign
    when (player.teleBlockCounter) {
        0 -> {
            if (blocked) {
                player.message("Your teleblock has worn off.")
            } else {
                player.message("Your teleblock resistance has worn off.")
            }
            cancel()
        }
        -1 -> player.message("Your teleblock resistance is about to wear off.")
        1 -> player.message("Your teleblock is about to wear off.")
    }
}