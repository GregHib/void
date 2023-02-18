package world.gregs.voidps.world.interact.entity.player.toxin

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

val Character.poisoned: Boolean get() = poisonCounter > 0

val Character.antipoison: Boolean get() = poisonCounter < 0

var Character.poisonCounter: Int
    get() = if (this is Player) getVar("poison", 0) else this["poison", 0]
    set(value) = if (this is Player) {
        setVar("poison", value)
    } else {
        this["poison"] = value
    }

fun Character.cure(): Boolean {
    val timers = if (this is Player) timers else softTimers
    timers.stop("poison")
    return true
}

fun Character.poison(target: Character, damage: Int) {
    if (damage < target["poison_damage", 0]) {
        return
    }
    val timers = if (target is Player) target.timers else target.softTimers
    if (timers.contains("poison") || timers.start("poison")) {
        target.poisonCounter = TimeUnit.SECONDS.toTicks(18)
        target["poison_damage", true] = damage
        target["poison_source"] = this
    }
}