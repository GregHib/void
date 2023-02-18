package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.inWilderness
import world.gregs.voidps.world.interact.entity.player.effect.skull
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick

on<CombatSwing>({ it.inWilderness && target is Player && !it.get<List<Character>>("attackers").contains(target) }) { player: Player ->
    player.skull()
}

on<TimerStart>({ timer == "skull" }) { player: Player ->
    player.appearance.skull = player.getVar("skull", 0)
    interval = player.getVar("skull_duration", 0)
    player.flagAppearance()
}

on<TimerTick>({ timer == "skull" }) { _: Player ->
    cancel()
}

on<TimerStop>({ timer == "skull" }) { player: Player ->
    player.appearance.skull = -1
    player.clearVar("skull")
    player.clearVar("skull_duration")
    player.flagAppearance()
}