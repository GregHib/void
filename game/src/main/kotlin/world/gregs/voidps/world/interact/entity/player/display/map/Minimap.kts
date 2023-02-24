package world.gregs.voidps.world.interact.entity.player.display.map

import world.gregs.voidps.engine.client.sendVarp
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.sendVariable
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on

on<InterfaceOpened>({ id == "health_orb" }) { player: Player ->
    player.set("life_points", player.levels.get(Skill.Constitution))
    player.sendVariable("poisoned")
}

on<InterfaceOpened>({ id == "summoning_orb" }) { player: Player ->
    player.sendVarp(1160, -1)
}