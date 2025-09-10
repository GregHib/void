package content.entity.effect

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.timer.characterTimerStart
import world.gregs.voidps.engine.timer.characterTimerStop
import world.gregs.voidps.engine.timer.characterTimerTick
import kotlin.math.sign
import world.gregs.voidps.engine.event.Script

import content.skill.prayer.praying
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player

val Character.frozen: Boolean get() = movementDelay > 0

val Character.frozenImmune: Boolean get() = movementDelay < 0

var Character.movementDelay: Int
    get() = if (this is Player) get("move_delay", 0) else this["move_delay", 0]
    set(value) = if (this is Player) {
        set("move_delay", value)
    } else {
        this["move_delay"] = value
    }

fun Character.freeze(target: Character, ticks: Int, force: Boolean = false): Boolean {
    if (target.frozen) {
        (this as? Player)?.message("Your target is already held by a magical force.")
        return false
    } else if (target.frozenImmune) {
        (this as? Player)?.message("The target is currently immune to that spell.")
        return false
    }
    (target as? Player)?.message("You have been frozen!")
    target.freeze(ticks, force)
    return true
}

fun Character.freeze(ticks: Int, force: Boolean = false) {
    val protect = praying("protect_from_magic") || praying("deflect_magic")
    movementDelay = if (force || !protect) ticks else ticks / 2
    softTimers.start("movement_delay")
}

fun Character.freezeImmune(ticks: Int) {
    movementDelay = -ticks
    softTimers.start("movement_delay")
}
@Script
class Freeze {

    init {
        characterTimerStart("movement_delay") { character ->
            character.start("movement_delay", -1)
            interval = 1
        }

        characterTimerTick("movement_delay") { character ->
            val frozen = character.frozen
            character.movementDelay -= character.movementDelay.sign
            if (character.movementDelay == 0) {
                if (frozen) {
                    character.movementDelay = -5
                } else {
                    cancel()
                }
            }
        }

        characterTimerStop("movement_delay") { character ->
            character.stop("movement_delay")
        }

    }

}
