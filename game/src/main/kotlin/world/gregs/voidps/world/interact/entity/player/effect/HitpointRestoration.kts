package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.CurrentLevelChanged
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.player.combat.prayer.praying
import java.util.concurrent.TimeUnit

on<Registered>({ player -> player.levels.getOffset(Skill.Constitution) < 0 }) { player: Player ->
    player.softTimers.start("restore_hitpoints")
}

on<CurrentLevelChanged>({ skill == Skill.Constitution && to > 0 && to < it.levels.getMax(skill) && !it.softTimers.contains("restore_hitpoints") }) { player: Player ->
    player.softTimers.start("restore_hitpoints")
}

on<TimerStart>({ timer == "restore_hitpoints" }) { _: Player ->
    interval = TimeUnit.SECONDS.toTicks(6)
}

on<TimerTick>({ timer == "restore_hitpoints" }) { player: Player ->
    if (player.levels.get(Skill.Constitution) == 0) {
        cancel()
        return@on
    }
    val total = player.levels.restore(Skill.Constitution, healAmount(player))
    if (total == 0) {
        cancel()
    }
}

/**
 * Default heal 20 every 12s for 100hp per min.
 */
fun healAmount(player: Player): Int {
    var heal = 1
    val movement = player["movement", "walk"]
    if (movement == "rest") {
        heal += 1
    } else if (movement == "music") {
        heal += 2
    } else if (player["dream", false]) {
        heal += 4
    }
    if (player.praying("rapid_renewal")) {
        heal += 5
    } else if (player.praying("rapid_heal") || player.equipped(EquipSlot.Cape).id.startsWith("constitution_cape")) {
        heal += 1
    }
    if (player.equipped(EquipSlot.Hands).id == "regen_bracelet") {
        heal *= 2
    }
    return heal
}