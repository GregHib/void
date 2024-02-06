package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.inWilderness

playerSpawn { player: Player ->
    if (player.skulled) {
        player.softTimers.restart("skull")
    }
}

combatSwing({ it.inWilderness && target is Player && it.get<List<Character>>("attackers")?.contains(target) != true }) { player: Player ->
    player.skull()
}

timerStart({ timer == "skull" }) { player: Player ->
    interval = 50
    player.appearance.skull = player["skull", 0]
    player.flagAppearance()
}

timerTick({ timer == "skull" }) { player: Player ->
    if (--player.skullCounter <= 0) {
        cancel()
        return@timerTick
    }
}

timerStop({ timer == "skull" }) { player: Player ->
    player.clear("skull")
    player.clear("skull_duration")
    player.appearance.skull = -1
    player.flagAppearance()
}