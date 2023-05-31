import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.world.spawn.loadNpcSpawns
import world.gregs.voidps.world.interact.world.spawn.loadObjectSpawns

val objectDefinitions: ObjectDefinitions by inject()
val npcs: NPCs by inject()

on<Registered> { world: World ->
    loadObjectSpawns(get(), objectDefinitions)
    loadNpcSpawns(npcs)
}