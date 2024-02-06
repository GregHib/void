package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.specialAttack
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.inv.itemChanged
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

fun isStaffOfLight(item: Item) = item.id.startsWith("staff_of_light")

itemChanged({ inventory == "worn_equipment" && index == EquipSlot.Weapon.index && isStaffOfLight(oldItem) }) { player: Player ->
    player.softTimers.stop("power_of_light")
}

combatHit({ it.softTimers.contains("power_of_light") }, Priority.LOW) { player: Player ->
    player.setGraphic("power_of_light_hit")
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

playerSpawn { player: Player ->
    if (player.contains("power_of_light")) {
        player.softTimers.restart("power_of_light")
    }
}

timerStart({ timer == "power_of_light" }) { _: Player ->
    interval = 1
}

timerTick({ timer == "power_of_light" }) { player: Player ->
    if (player.dec("power_of_light") <= 0) {
        cancel()
    }
}

timerStop({ timer == "power_of_light" }) { player: Player ->
    player.message("<red>The power of the light fades. Your resistance to melee attacks returns to normal.")
    player.clear("power_of_light")
}