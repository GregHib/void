package rs.dusk.world.interact.entity.npc.spawn

import rs.dusk.engine.entity.Registered
import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.character.IndexAllocator
import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.npc.NPCRegistered
import rs.dusk.engine.entity.character.npc.NPCs
import rs.dusk.engine.entity.character.update.visual.npc.turn
import rs.dusk.engine.entity.definition.NPCDefinitions
import rs.dusk.engine.entity.list.MAX_NPCS
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.map.collision.Collisions
import rs.dusk.engine.path.TraversalType
import rs.dusk.engine.path.strat.DistanceTargetStrategy
import rs.dusk.engine.path.strat.RectangleTargetStrategy
import rs.dusk.engine.path.traverse.LargeTraversal
import rs.dusk.engine.path.traverse.MediumTraversal
import rs.dusk.engine.path.traverse.SmallTraversal
import rs.dusk.utility.inject

val npcs: NPCs by inject()
val bus: EventBus by inject()
val indexer = IndexAllocator(MAX_NPCS)
val definitions: NPCDefinitions by inject()

NPCSpawn then {
    val definition = definitions.get(id)
    val size = Size(definition.size, definition.size)
    val npc = NPC(id, tile, size)
    val collisions: Collisions = rs.dusk.utility.get()
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