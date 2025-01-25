package content.skill.melee.weapon.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.combatHit
import content.entity.player.combat.special.SpecialAttack
import content.entity.player.combat.special.specialAttackPrepare
import java.util.concurrent.TimeUnit

itemRemoved("staff_of_light*", EquipSlot.Weapon, "worn_equipment") { player ->
    player.softTimers.stop("power_of_light")
}

combatHit { player ->
    if (player.softTimers.contains("power_of_light")) {
        player.gfx("power_of_light_hit")
    }
}

specialAttackPrepare("power_of_light") { player ->
    cancel()
    if (!SpecialAttack.drain(player)) {
        return@specialAttackPrepare
    }
    player.anim("${id}_special")
    player.gfx("${id}_special")
    player[id] = TimeUnit.MINUTES.toTicks(1)
    player.softTimers.start(id)
}

playerSpawn { player ->
    if (player.contains("power_of_light")) {
        player.softTimers.restart("power_of_light")
    }
}

timerStart("power_of_light") {
    interval = 1
}

timerTick("power_of_light") { player ->
    if (player.dec("power_of_light") <= 0) {
        cancel()
    }
}

timerStop("power_of_light") { player ->
    player.message("<red>The power of the light fades. Your resistance to melee attacks returns to normal.")
    player.clear("power_of_light")
}