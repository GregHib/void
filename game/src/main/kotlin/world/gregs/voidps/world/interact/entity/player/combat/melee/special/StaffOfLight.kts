package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.ItemChanged
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.CombatHit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import java.util.concurrent.TimeUnit

fun isStaffOfLight(item: Item) = item.id.startsWith("staff_of_light")

on<ItemChanged>({ inventory == "worn_equipment" && index == EquipSlot.Weapon.index && isStaffOfLight(oldItem) }) { player: Player ->
    player.softTimers.stop("power_of_light")
}

on<CombatHit>({ it.softTimers.contains("power_of_light") }, Priority.LOW) { player: Player ->
    player.setGraphic("power_of_light_hit")
}

// Special attack

on<VariableSet>({ key == "special_attack" && to == true && isStaffOfLight(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK)) {
        return@on
    }
    player.setAnimation("power_of_light")
    player.setGraphic("power_of_light")
    player["power_of_light"] = TimeUnit.MINUTES.toTicks(1)
    player.softTimers.start("power_of_light")
    player.specialAttack = false
}

on<Registered>({ it.contains("power_of_light") }) { player: Player ->
    player.softTimers.restart("power_of_light")
}

on<TimerStart>({ timer == "power_of_light" }) { _: Player ->
    interval = 1
}

on<TimerTick>({ timer == "power_of_light" }) { player: Player ->
    if (player.dec("power_of_light") <= 0) {
        cancel()
    }
}

on<TimerStop>({ timer == "power_of_light" }) { player: Player ->
    player.message("<red>The power of the light fades. Your resistance to melee attacks returns to normal.")
    player.clear("power_of_light")
}