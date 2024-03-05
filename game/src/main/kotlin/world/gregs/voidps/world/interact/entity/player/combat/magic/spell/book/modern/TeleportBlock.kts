package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.player.combat.prayer.protectMagic
import world.gregs.voidps.world.interact.entity.proj.shoot
import kotlin.math.sign

val definitions: SpellDefinitions by inject()

combatSwing(spell = "teleport_block", style = "magic") { player ->
    if (target is NPC) {
        delay = -1
        player.message("You can't use that against an NPC.")
        return@combatSwing
    }
    val spell = player.spell
    player.setAnimation("${spell}_cast")
    player.setGraphic("${spell}_cast")
    player.shoot(id = player.spell, target = target)
    val distance = player.tile.distanceTo(target)
    if (player.hit(target, delay = Hit.magicDelay(distance)) != -1) {
        val duration: Int = definitions.get(player.spell)["block_ticks"]
        player.teleBlock(target, duration)
    }
    delay = 5
}

timerStart("teleport_block") { player ->
    if (player.teleBlockImmune) {
        cancel()
        return@timerStart
    }
    if (player.protectMagic()) {
        player.teleBlockCounter /= 2
    }
    if (!restart) {
        player.message("You have been teleblocked.")
    }
    interval = 50
}

timerTick("teleport_block") { player ->
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