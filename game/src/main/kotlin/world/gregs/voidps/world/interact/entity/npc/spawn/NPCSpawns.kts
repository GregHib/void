package world.gregs.voidps.world.interact.entity.npc.spawn

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.IndexAllocator
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCRegistered
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.update.visual.npc.turn
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.list.MAX_NPCS
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.TraversalType
import world.gregs.voidps.engine.path.strat.DistanceTargetStrategy
import world.gregs.voidps.engine.path.strat.RectangleTargetStrategy
import world.gregs.voidps.engine.path.traverse.LargeTraversal
import world.gregs.voidps.engine.path.traverse.MediumTraversal
import world.gregs.voidps.engine.path.traverse.SmallTraversal
import world.gregs.voidps.utility.inject

val npcs: NPCs by inject()
val bus: EventBus by inject()
val indexer = IndexAllocator(MAX_NPCS)
val definitions: NPCDefinitions by inject()

NPCSpawn then {
    val definition = definitions.get(id)
    val size = Size(definition.size, definition.size)
    val npc = NPC(id, tile, size)
    val collisions: Collisions = world.gregs.voidps.utility.get()
    npc.movement.traversal = when (definition.size) {
        1 -> SmallTraversal(TraversalType.Land, true, collisions)
        2 -> MediumTraversal(TraversalType.Land, true, collisions)
        else -> LargeTraversal(TraversalType.Land, true, size, collisions)
    }

    npc.interactTarget = if(definition.name.contains("banker", true)) {
        DistanceTargetStrategy(1, npc.tile.add(direction.delta))
    } else {
        RectangleTargetStrategy(collisions, npc)
    }
    npc.index = indexer.obtain() ?: return@then
    npc.turn(direction.delta.x, direction.delta.y)
    bus.emit(NPCRegistered(npc))
    npcs.add(npc)
    bus.emit(Registered(npc))
    result = npc
}

NPCDespawn then {
    npcs.remove(npc)
}