package world.gregs.voidps.engine.entity.character.npc

import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.IndexAllocator
import world.gregs.voidps.engine.entity.character.update.visual.npc.turn
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.list.MAX_NPCS
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.TraversalType
import world.gregs.voidps.engine.path.strat.DistanceTargetStrategy
import world.gregs.voidps.engine.path.strat.RectangleTargetStrategy
import world.gregs.voidps.engine.path.traverse.LargeTraversal
import world.gregs.voidps.engine.path.traverse.MediumTraversal
import world.gregs.voidps.engine.path.traverse.SmallTraversal

val npcLoaderModule = module {
    single { NPCFactory(get(), get(), get(), get()) }
}

class NPCFactory(
    private val npcs: NPCs,
    private val definitions: NPCDefinitions,
    private val collisions: Collisions,
    private val store: EventHandlerStore
) {
    private val indexer = IndexAllocator(MAX_NPCS)
    private val logger = InlineLogger()

    fun spawn(name: String, tile: Tile, direction: Direction = Direction.NONE): NPC? {
        return spawn(name, Rectangle(tile.x, tile.y, tile.x, tile.y), direction)
    }

    fun spawn(id: Int, tile: Tile, direction: Direction = Direction.NONE): NPC? {
        return spawn(id, Rectangle(tile.x, tile.y, tile.x, tile.y), direction)
    }

    fun spawn(name: String, area: Area, direction: Direction): NPC? {
        return spawn(definitions.getId(name), area, direction)
    }

    fun spawn(id: Int, area: Area, direction: Direction): NPC? {
        val definition = definitions.get(id)
        val size = Size(definition.size, definition.size)
        val traversal = when (definition.size) {
            1 -> SmallTraversal(TraversalType.Land, true, collisions)
            2 -> MediumTraversal(TraversalType.Land, true, collisions)
            else -> LargeTraversal(TraversalType.Land, true, size, collisions)
        }
        val tile = area.random(traversal)
        if (tile == null) {
            logger.warn { "No free area found for npc spawn $id $area" }
            return null
        }
        val npc = NPC(id, tile, size)
        store.populate(npc)
        npc.movement.traversal = traversal
        val dir = if (direction == Direction.NONE) Direction.all.random() else direction
        npc.interactTarget = if (definition.name.contains("banker", true)) {
            DistanceTargetStrategy(1, npc.tile.add(dir.delta))
        } else {
            RectangleTargetStrategy(collisions, npc)
        }
        npc.index = indexer.obtain() ?: return null
        npc.turn(dir.delta.x, dir.delta.y)
        collisions.add(npc)
        npcs.add(npc)
        npc.events.emit(Registered)
        return npc
    }

    fun despawn(npc: NPC) {
        npcs.remove(npc)
        collisions.remove(npc)
    }
}