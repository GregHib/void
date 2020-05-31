package rs.dusk.engine.client.update.task

import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.engine.task.EntityTask
import rs.dusk.engine.model.entity.index.Move
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.npc.NPCs
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class NPCPostUpdateTask(override val entities: NPCs, private val bus: EventBus) : EntityTask<NPC>() {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun runAsync(npc: NPC) {
        if (npc.movement.delta != Tile.EMPTY) {
            npc.movement.lastTile = npc.tile
            entities.remove(npc.tile, npc)
            entities.remove(npc.tile.chunk, npc)
            npc.tile = npc.tile.add(npc.movement.delta)
            entities.add(npc.tile, npc)
            entities.add(npc.tile.chunk, npc)
            bus.emit(Move(npc, npc.movement.lastTile, npc.tile))
        }
        npc.movement.reset()
        npc.visuals.aspects.forEach { (_, visual) ->
            visual.reset(npc)
        }
    }

}