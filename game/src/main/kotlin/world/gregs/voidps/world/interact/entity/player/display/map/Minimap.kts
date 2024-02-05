package world.gregs.voidps.world.interact.entity.player.display.map

import world.gregs.voidps.engine.client.ui.event.interfaceOpened
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

interfaceOpened({ id == "health_orb" }) { player: Player ->
    player["life_points"] = player.levels.get(Skill.Constitution)
    player.sendVariable("poisoned")
}

interfaceOpened({ id == "summoning_orb" }) { player: Player ->
    player.sendVariable("show_summoning_orb")
}