package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.specialAttack
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.combatHit
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import java.util.concurrent.TimeUnit

itemRemoved("staff_of_light*", EquipSlot.Weapon, "worn_equipment") { player ->
    player.softTimers.stop("power_of_light")
}

combatHit { player ->
    if (player.softTimers.contains("power_of_light")) {
        player.setGraphic("power_of_light_hit")
    }
}

// Special attack

specialAttack("staff_of_light*") { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK)) {
        return@specialAttack
    }
    player.setAnimation("power_of_light")
    player.setGraphic("power_of_light")
    player["power_of_light"] = TimeUnit.MINUTES.toTicks(1)
    player.softTimers.start("power_of_light")
    player.specialAttack = false
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