package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.inWilderness

on<Registered>({ it.skulled }) { player: Player ->
    player.softTimers.restart("skull")
}

on<CombatSwing>({ it.inWilderness && target is Player && !it.get<List<Character>>("attackers").contains(target) }) { player: Player ->
    player.skull()
}

on<TimerStart>({ timer == "skull" }) { player: Player ->
    interval = 50
    player.appearance.skull = player.getVar("skull", 0)
    player.flagAppearance()
}

on<TimerTick>({ timer == "skull" }) { player: Player ->
    if (--player.skullCounter <= 0) {
        return@on cancel()
    }
}

on<TimerStop>({ timer == "skull" }) { player: Player ->
    player.clearVar("skull")
    player.clearVar("skull_duration")
    player.appearance.skull = -1
    player.flagAppearance()
}