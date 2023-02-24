package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.world.activity.combat.prayer.prayerActive

val Character.frozen: Boolean get() = movementDelay > 0

val Character.frozenImmune: Boolean get() = movementDelay < 0

var Character.movementDelay: Int
    get() = if (this is Player) getVar("movement_delay", 0) else this["movement_delay", 0]
    set(value) = if (this is Player) {
        set("movement_delay", value)
    } else {
        this["movement_delay"] = value
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
    val protect = prayerActive("protect_from_magic") || prayerActive("deflect_magic")
    movementDelay = if (force || !protect) ticks else ticks / 2
    softTimers.start("movement_delay")
}

fun Character.freezeImmune(ticks: Int) {
    movementDelay = -ticks
    softTimers.start("movement_delay")
}