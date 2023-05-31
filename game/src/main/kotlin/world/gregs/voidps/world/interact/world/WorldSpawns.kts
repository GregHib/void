package world.gregs.voidps.world.interact.world

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.world.spawn.loadNpcSpawns
import world.gregs.voidps.world.interact.world.spawn.loadObjectSpawns

val npcs: NPCs by inject()

on<Registered> { _: World ->
    loadObjectSpawns(get())
    loadNpcSpawns(npcs)
}