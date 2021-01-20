package world.gregs.void.world.interact.entity.npc.spawn

import world.gregs.void.engine.entity.Registered
import world.gregs.void.engine.entity.Size
import world.gregs.void.engine.entity.character.IndexAllocator
import world.gregs.void.engine.entity.character.npc.NPC
import world.gregs.void.engine.entity.character.npc.NPCRegistered
import world.gregs.void.engine.entity.character.npc.NPCs
import world.gregs.void.engine.entity.character.update.visual.npc.turn
import world.gregs.void.engine.entity.definition.NPCDefinitions
import world.gregs.void.engine.entity.list.MAX_NPCS
import world.gregs.void.engine.event.EventBus
import world.gregs.void.engine.event.then
import world.gregs.void.engine.map.collision.Collisions
import world.gregs.void.engine.path.TraversalType
import world.gregs.void.engine.path.strat.DistanceTargetStrategy
import world.gregs.void.engine.path.strat.RectangleTargetStrategy
import world.gregs.void.engine.path.traverse.LargeTraversal
import world.gregs.void.engine.path.traverse.MediumTraversal
import world.gregs.void.engine.path.traverse.SmallTraversal
import world.gregs.void.utility.inject

val npcs: NPCs by inject()
val bus: EventBus by inject()
val indexer = IndexAllocator(MAX_NPCS)
val definitions: NPCDefinitions by inject()

NPCSpawn then {
    val definition = definitions.get(id)
    val size = Size(definition.size, definition.size)
    val npc = NPC(id, tile, size)
    val collisions: Collisions = world.gregs.void.utility.get()
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