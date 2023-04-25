package world.gregs.voidps.engine.entity.character.npc

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.data.definition.extra.NPCDefinitions
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.Collisions

data class NPCs(
    private val definitions: NPCDefinitions,
    private val collisions: Collisions,
    private val store: EventHandlerStore,
    private val collision: CollisionStrategyProvider
) : CharacterList<NPC>(MAX_NPCS) {
    override val indexArray: Array<NPC?> = arrayOfNulls(MAX_NPCS)
    private val logger = InlineLogger()

    init {
        Wander.active = getProperty("randomWalk") == "true"
    }

    fun add(id: String, tile: Tile, direction: Direction = Direction.NONE, delay: Int? = null): NPC? {
        val npc = add(id, tile, direction) ?: return null
        val respawnDelay = delay ?: npc.def.getOrNull("respawn_delay")
        if (respawnDelay != null && respawnDelay > 0) {
            npc["respawn_tile"] = tile
            npc["respawn_delay"] = respawnDelay
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
        npc.levels.clear()
        npc["spawn_tile"] = tile
        if (Wander.wanders(npc)) {
            npc.mode = Wander(npc, tile)
        }
        store.populate(npc)
        val dir = if (direction == Direction.NONE) Direction.all.random() else direction
        npc.index = indexer.obtain() ?: return null
        npc.face(dir)
        npc.collision = collision.get(npc)
        super.add(npc)
        return npc
    }

    fun releaseIndex(npc: NPC) {
        if (npc.index > 0) {
            indexer.release(npc.index)
        }
    }

    override fun clear() {
        for (npc in this) {
            npc.events.emit(Unregistered)
        }
        super.clear()
        indexer.clear()
    }
}