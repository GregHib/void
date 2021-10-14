package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.start

fun Character.freeze(target: Character, ticks: Int, force: Boolean = false) {
    if (target.hasEffect("freeze")) {
        (this as? Player)?.message("Your target is already held by a magical force.")
    } else if (target.hasEffect("bind_immunity")) {
        (this as? Player)?.message("The target is currently immune to that spell.")
    } else {
        val protect = target.hasEffect("prayer_deflect_magic") || target.hasEffect("prayer_protect_from_magic")
        target.start("freeze", if(force || !protect) ticks else ticks / 2)
    }
}