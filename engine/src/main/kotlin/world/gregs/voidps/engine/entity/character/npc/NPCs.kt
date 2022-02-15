package world.gregs.voidps.engine.entity.character.npc

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.IndexAllocator
import world.gregs.voidps.engine.entity.character.update.visual.npc.turn
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.list.MAX_NPCS
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.strat.FollowTargetStrategy
import world.gregs.voidps.engine.path.strat.RectangleTargetStrategy

data class NPCs(
    private val definitions: NPCDefinitions,
    private val collisions: Collisions,
    private val store: EventHandlerStore
) : CharacterList<NPC>(MAX_NPCS) {
    override val indices: Array<NPC?> = arrayOfNulls(MAX_NPCS)
    private val indexer = IndexAllocator(MAX_NPCS)
    private val logger = InlineLogger()

    fun add(id: String, tile: Tile, direction: Direction = Direction.NONE, delay: Int = 60): NPC? {
        val npc = add(id, tile, direction) ?: return null
        if (delay >= 0) {
            npc["respawn_tile"] = tile
            npc["respawn_delay"] = delay
            npc["respawn_direction"] = direction
        }
        npc.events.emit(Registered)
        return npc
    }

    private fun add(id: String, tile: Tile, direction: Direction = Direction.NONE): NPC? {
        val def = definitions.get(id)
        if (def.id == -1) {
            logger.warn { "No npc found for name $id" }
            return null
        }
        val npc = NPC(id, tile, Size(def.size, def.size))
        npc.levels.link(npc.events, NPCLevels(def))
        npc["spawn_tile"] = tile
        store.populate(npc)
        val dir = if (direction == Direction.NONE) Direction.all.random() else direction
        npc.interactTarget = RectangleTargetStrategy(collisions, npc, allowUnder = false)
        npc.index = indexer.obtain() ?: return null
        npc.turn(dir.delta.x, dir.delta.y)
        npc.followTarget = FollowTargetStrategy(npc)
        collisions.add(npc)
        super.add(npc)
        return npc
    }

    override fun remove(element: NPC): Boolean {
        collisions.remove(element)
        if (element.index > 0) {
            indexer.release(element.index)
        }
        return super.remove(element)
    }

    override fun clear() {
        super.clear()
        indexer.clear()
    }
}