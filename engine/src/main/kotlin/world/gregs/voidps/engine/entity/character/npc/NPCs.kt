package world.gregs.voidps.engine.entity.character.npc

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.list.MAX_NPCS
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.strat.FollowTargetStrategy
import world.gregs.voidps.engine.path.strat.RectangleTargetStrategy
import world.gregs.voidps.engine.path.traverse.*

data class NPCs(
    private val definitions: NPCDefinitions,
    private val collisions: Collisions,
    private val store: EventHandlerStore,
    private val collision: CollisionStrategyProvider
) : CharacterList<NPC>(MAX_NPCS) {
    override val indexArray: Array<NPC?> = arrayOfNulls(MAX_NPCS)
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
        npc.def = def
        npc.levels.link(npc.events, NPCLevels(def))
        npc["spawn_tile"] = tile
        store.populate(npc)
        val dir = if (direction == Direction.NONE) Direction.all.random() else direction
        npc.interactTarget = RectangleTargetStrategy(collisions, npc, allowUnder = false)
        npc.index = indexer.obtain() ?: return null
        npc.turn(dir.delta.x, dir.delta.y)
        npc.followTarget = FollowTargetStrategy(npc)
        npc.collision = collision.get(npc)
        npc.traversal = getTraversal(npc)
        collisions.add(npc)
        super.add(npc)
        return npc
    }

    private fun getTraversal(npc: NPC): TileTraversalStrategy {
        return when {
            npc.def["swim", false] -> SwimTraversal
            npc.size == Size.ONE -> SmallTraversal
            npc.size.width == 2 && npc.size.height == 2 -> MediumTraversal
            else -> LargeTraversal
        }
    }

    override fun remove(element: NPC): Boolean {
        collisions.remove(element)
        if (element.index > 0) {
            indexer.release(element.index)
        }
        return super.remove(element)
    }

    override fun clear() {
        for (npc in this) {
            collisions.remove(npc)
        }
        super.clear()
        indexer.clear()
    }
}