package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOpened>({ name == "quest_journals"}) { player: Player ->
    player.sendVar("quest_points")
    player.sendVar("unstable_foundations")
}