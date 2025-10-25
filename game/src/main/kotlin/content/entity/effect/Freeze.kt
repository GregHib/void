package content.entity.effect

import content.skill.prayer.praying
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.*
import kotlin.math.sign

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
class Freeze : Api {

    @Timer("movement_delay")
    override fun start(character: Character, timer: String, restart: Boolean): Int {
        character.start("movement_delay", -1)
        return 1
    }

    @Timer("movement_delay")
    override fun tick(character: Character, timer: String): Int {
        val frozen = character.frozen
        character.movementDelay -= character.movementDelay.sign
        if (character.movementDelay == 0) {
            if (frozen) {
                character.movementDelay = -5
            } else {
                return Timer.CANCEL
            }
        }
        return Timer.CONTINUE
    }

    @Timer("movement_delay")
    override fun stop(character: Character, timer: String, logout: Boolean) {
        character.stop("movement_delay")
    }
}
