package content.entity.effect.toxin

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

val Character.diseased: Boolean get() = diseaseCounter > 0

val Character.antiDisease: Boolean get() = diseaseCounter < 0

var Character.diseaseCounter: Int
    get() = if (this is Player) get("disease", 0) else this["disease", 0]
    set(value) = if (this is Player) {
        set("disease", value)
    } else {
        this["disease"] = value
    }

fun Character.cureDisease(): Boolean {
    val timers = if (this is Player) timers else softTimers
    timers.stop("disease")
    return true
}

fun Character.disease(target: Character, damage: Int) {
    if (damage < target["disease_damage", 0]) {
        return
    }
    val timers = if (target is Player) target.timers else target.softTimers
    if (timers.contains("disease") || timers.start("disease")) {
        target.diseaseCounter = TimeUnit.SECONDS.toTicks(18) / 30
        target["disease_damage"] = damage
        target["disease_source"] = this
    }
}

fun Player.antiDisease(minutes: Int) = antiDisease(minutes, TimeUnit.MINUTES)

fun Player.antiDisease(duration: Int, timeUnit: TimeUnit) {
    diseaseCounter = -(timeUnit.toTicks(duration) / 30)
    clear("disease_damage")
    clear("disease_source")
    timers.startIfAbsent("disease")
}