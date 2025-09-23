package content.entity.effect

import content.entity.combat.hit.directHit
import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player

val Character.stunned: Boolean get() = hasClock("stunned")

val Character.stunImmune: Boolean get() = this["immune_stun", false]

fun Character.stun(target: Character, ticks: Int, hit: Int = -1): Boolean {
    if (target.stunned) {
        (this as? Player)?.message("This target is already stunned.") // TODO
        return false
    } else if (target.stunImmune) {
        (this as? Player)?.message("The target is immune to being stunned.") // TODO
        return false
    }
    if (hit != -1) {
        target.directHit(hit)
    }
    target.gfx("stun_long")
    target.sound("stun")
    target.message("You've been stunned!")
    target["delay"] = ticks
    target.start("stunned", ticks)
    target.start("movement_delay", ticks)
    return true
}
