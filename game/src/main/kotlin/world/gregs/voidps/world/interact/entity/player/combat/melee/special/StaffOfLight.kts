package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Red
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.contain.ItemChanged
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import java.util.concurrent.TimeUnit
import kotlin.math.floor

fun isStaffOfLight(item: Item?) = item != null && item.id.startsWith("staff_of_light")

on<ItemChanged>({ container == "worn_equipment" && index == EquipSlot.Weapon.index && isStaffOfLight(oldItem) }) { player: Player ->
    player.softTimers.stop("power_of_light")
}

on<CombatSwing>({ !swung() && isStaffOfLight(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("staff_of_light_${player.attackType}")
    player.hit(target)
    delay = 6
}

on<CombatAttack>({ !blocked && target is Player && isStaffOfLight(target.weapon) }) { _: Character ->
    target.setAnimation("staff_of_light_block", delay)
    blocked = true
}

on<CombatHit>({ it.softTimers.contains("power_of_light") }, Priority.LOW) { player: Player ->
    player.setGraphic("power_of_light_hit")
}

// Special attack

on<HitDamageModifier>({ type == "melee" && target != null && target.softTimers.contains("power_of_light") }, Priority.HIGH) { _: Player ->
    damage = floor(damage * 0.5)
}

on<VariableSet>({ key == "special_attack" && to == true && isStaffOfLight(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK)) {
        return@on
    }
    player.setAnimation("power_of_light")
    player.setGraphic("power_of_light")
    player.setVar("power_of_light", TimeUnit.MINUTES.toTicks(1))
    player.softTimers.start("power_of_light")
    player.specialAttack = false
}

on<Registered>({ it.hasVar("power_of_light") }) { player: Player ->
    player.softTimers.restart("power_of_light")
}

on<TimerStart>({ timer == "power_of_light" }) { _: Player ->
    interval = 1
}

on<TimerTick>({ timer == "power_of_light" }) { player: Player ->
    if (player.decVar("power_of_light") <= 0) {
        cancel()
    }
}

on<TimerStop>({ timer == "power_of_light" }) { player: Player ->
    player.message(Red { "The power of the light fades. Your resistance to melee attacks returns to normal." })
    player.clearVar("power_of_light")
}