package world.gregs.voidps.world.interact.entity.player.display.map

import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.sendVarp

on<InterfaceOpened>({ name == "health_orb" }) { player: Player ->
    player.setVar("life_points", player.levels.get(Skill.Constitution))
    player.sendVar("poisoned")
}

on<InterfaceOpened>({ name == "summoning_orb" }) { player: Player ->
    player.sendVarp(1160, -1)
}