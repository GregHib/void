import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.world.spawn.loadItemSpawns
import world.gregs.voidps.world.interact.world.spawn.loadNpcSpawns
import world.gregs.voidps.world.interact.world.spawn.loadObjectSpawns

val objectDefinitions: ObjectDefinitions by inject()
val npcs: NPCs by inject()
val items: FloorItems by inject()

on<Registered> { world: World ->
    loadObjectSpawns(get(), objectDefinitions)
    loadNpcSpawns(npcs)
    loadItemSpawns(items)
}