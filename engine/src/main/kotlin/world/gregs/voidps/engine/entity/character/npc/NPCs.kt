package world.gregs.voidps.engine.entity.character.npc

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.MAX_NPCS
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.CharacterMap
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.RegionLevel
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

data class NPCs(
    private val definitions: NPCDefinitions,
    private val collisions: Collisions,
    private val store: EventHandlerStore,
    private val collision: CollisionStrategyProvider
) : CharacterList<NPC>(MAX_NPCS) {
    override val indexArray: Array<NPC?> = arrayOfNulls(MAX_NPCS)
    private val logger = InlineLogger()
    private val map: CharacterMap = CharacterMap()

    init {
        Wander.active = getProperty("randomWalk") == "true"
    }

    override operator fun get(tile: Tile): List<NPC> {
        return get(tile.regionLevel).filter { it.tile == tile }
    }

    override operator fun get(zone: Zone): List<NPC> {
        return get(zone.regionLevel).filter { it.tile.zone == zone }
    }

    operator fun get(region: RegionLevel): List<NPC> {
        val list = mutableListOf<NPC>()
        for (index in map[region] ?: return list) {
            list.add(indexed(index) ?: continue)
        }
        return list
    }

    override fun add(element: NPC): Boolean {
        if (super.add(element)) {
            map.add(element.tile.regionLevel, element)
            return true
        }
        return false
    }

    fun update(from: Tile, to: Tile, element: NPC) {
        if (from.regionLevel != to.regionLevel) {
            map.remove(from.regionLevel, element)
            map.add(to.regionLevel, element)
        }
    }

    fun clear(region: RegionLevel) {
        for (index in map[region] ?: return) {
            val element = indexed(index) ?: continue
            super.remove(element)
            removeIndex(element)
            releaseIndex(element)
        }
    }

    override fun remove(element: NPC): Boolean {
        map.remove(element.tile.regionLevel, element)
        return super.remove(element)
    }

    fun getDirect(region: RegionLevel): List<Int>? = this.map[region]

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
        val npc = NPC(id, tile)
        npc.def = def
        npc.levels.link(npc.events, NPCLevels(def))
        npc.levels.clear(Skill.Constitution)
        npc.levels.clear(Skill.Attack)
        npc.levels.clear(Skill.Strength)
        npc.levels.clear(Skill.Defence)
        npc.levels.clear(Skill.Ranged)
        npc.levels.clear(Skill.Magic)
        npc["spawn_tile"] = tile
        if (Wander.wanders(npc)) {
            npc.mode = Wander(npc, tile)
        }
        store.populate(npc)
        val dir = if (direction == Direction.NONE) Direction.all.random() else direction
        npc.index = indexer.obtain() ?: return null
        npc.face(dir)
        npc.collision = collision.get(npc)
        add(npc)
        return npc
    }

    override fun clear() {
        for (npc in this) {
            npc.events.emit(Unregistered)
        }
        super.clear()
        indexer.clear()
    }
}