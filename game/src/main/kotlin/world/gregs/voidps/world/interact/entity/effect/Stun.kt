package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.hit
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.login.protocol.visual.update.Hitsplat

val Character.stunned: Boolean get() = hasClock("stunned")

val Character.stunImmune: Boolean get() = this["immune_stun", false]

fun Character.stun(target: Character, ticks: Int, hit: Int = -1): Boolean {
    if (target.stunned) {
        (this as? Player)?.message("Your target is already stunned.") // TODO
        return false
    } else if (target.stunImmune) {
        (this as? Player)?.message("The target is currently immune to being stunned.") // TODO
        return false
    }
    if (hit != -1) {
        target.hit(this, hit, Hitsplat.Mark.Regular)
    }
    (target as? Player)?.message("You've' been stunned!")
    start("stunned", ticks)
    start("movement_delay", ticks)
    return true
}