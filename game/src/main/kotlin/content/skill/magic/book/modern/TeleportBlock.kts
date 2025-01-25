package content.skill.magic.book.modern

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerTick
import content.entity.combat.combatPrepare
import content.skill.magic.spell.spell
import content.skill.prayer.protectMagic
import kotlin.math.sign

combatPrepare("magic") { player ->
    if (player.spell == "teleport_block" && target is NPC) {
        player.message("You can't use that against an NPC.")
        cancel()
    }
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