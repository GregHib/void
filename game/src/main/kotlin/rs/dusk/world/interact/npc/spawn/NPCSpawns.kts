package rs.dusk.world.interact.npc.spawn

import rs.dusk.cache.definition.decoder.NPCDecoder
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.character.IndexAllocator
import rs.dusk.engine.model.entity.character.npc.NPC
import rs.dusk.engine.model.entity.character.npc.NPCRegistered
import rs.dusk.engine.model.entity.character.npc.NPCs
import rs.dusk.engine.model.entity.character.update.visual.npc.turn
import rs.dusk.engine.model.entity.list.MAX_NPCS
import rs.dusk.engine.model.map.collision.Collisions
import rs.dusk.engine.path.TraversalType
import rs.dusk.engine.path.traverse.LargeTraversal
import rs.dusk.engine.path.traverse.MediumTraversal
import rs.dusk.engine.path.traverse.SmallTraversal
import rs.dusk.utility.inject

val npcs: NPCs by inject()
val bus: EventBus by inject()
val indexer = IndexAllocator(MAX_NPCS)
val decoder: NPCDecoder by inject()

NPCSpawn then {
    val definition = decoder.getSafe(id)
    val size = Size(definition.size, definition.size)
    val npc = NPC(id, tile, size)
    val collisions: Collisions = rs.dusk.utility.get()
    npc.movement.traversal = when (definition.size) {
        1 -> SmallTraversal(TraversalType.Land, true, collisions)
        2 -> MediumTraversal(TraversalType.Land, true, collisions)
        else -> LargeTraversal(TraversalType.Land, true, size, collisions)
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