package world.gregs.voidps.engine.entity.character.npc

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.MAX_NPCS
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.character.CharacterMap
import world.gregs.voidps.engine.entity.character.CharacterSearch
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.RegionLevel
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

data class NPCs(
    private val definitions: NPCDefinitions,
    private val collisions: Collisions,
    private val collision: CollisionStrategyProvider,
    private val areaDefinitions: AreaDefinitions,
) : Runnable,
    Iterable<NPC>,
    CharacterSearch<NPC> {
    private val indexArray: Array<NPC?> = arrayOfNulls(MAX_NPCS)
    private var indexer = 1
    private val spawnQueue: Array<NPC?> = arrayOfNulls(MAX_NPCS)
    private var spawnIndex = 0
    private val removeQueue: IntArray = IntArray(MAX_NPCS)
    private var removeIndex = 0
    var size = 0
        private set
    private val map: CharacterMap = CharacterMap()
    private val logger = InlineLogger()

    override fun run() {
        var i = 0
        while (i < removeIndex) {
            val index = removeQueue[i]
            removeQueue[i++] = -1
            size--
            val npc = indexArray[index] ?: continue
            indexArray[index] = null
            map.remove(npc.tile.regionLevel, npc)
            npc.index = -1
        }
        removeIndex = 0
        i = 0
        while (i < spawnIndex) {
            val npc = spawnQueue[i++] ?: continue
            if (!spawn(npc)) {
                logger.warn { "Failed to spawn $npc" }
            } else {
                size++
            }
            spawnQueue[i - 1] = null
        }
        spawnIndex = 0
    }

    fun indexed(index: Int): NPC? = indexArray.getOrNull(index)

    fun add(id: String, tile: Tile, direction: Direction = Direction.NONE): NPC {
        val def = definitions.getOrNull(id) ?: return NPC(id, tile, NPCDefinition.EMPTY)
        val npc = NPC(id, tile, def)
        if (spawnIndex < spawnQueue.size) {
            spawnQueue[spawnIndex++] = npc
        }
        val dir = if (direction == Direction.NONE) Direction.all.random() else direction
        npc.face(dir)
        return npc
    }

    fun remove(npc: NPC?): Boolean {
        if (npc == null || npc.index == -1) {
            logger.warn { "Unable to remove npc $npc." }
            return false
        }
        if (removeIndex < removeQueue.size) {
            npc.hide = true
            removeQueue[removeIndex++] = npc.index
            return true
        }
        return false
    }

    fun update(from: Tile, to: Tile, npc: NPC) {
        if (from.regionLevel != to.regionLevel) {
            map.remove(from.regionLevel, npc)
            map.add(to.regionLevel, npc)
        }
    }

    fun getDirect(region: RegionLevel): List<Int>? = this.map[region]

    override operator fun get(tile: Tile): List<NPC> {
        val list = mutableListOf<NPC>()
        for (index in map[tile.regionLevel] ?: return list) {
            val npc = indexed(index) ?: continue
            if (npc.tile == tile) {
                list.add(npc)
            }
        }
        return list
    }

    override operator fun get(zone: Zone): List<NPC> {
        val list = mutableListOf<NPC>()
        for (index in map[zone.regionLevel] ?: return list) {
            val npc = indexed(index) ?: continue
            if (npc.tile.zone == zone) {
                list.add(npc)
            }
        }
        return list
    }

    operator fun get(region: RegionLevel): List<NPC> {
        val list = mutableListOf<NPC>()
        for (index in map[region] ?: return list) {
            list.add(indexed(index) ?: continue)
        }
        return list
    }

    private fun index(): Int? {
        if (indexer < indexArray.size) {
            return indexer++
        }
        for (i in 1 until indexArray.size) {
            if (indexArray[i] == null) {
                return i
            }
        }
        return null
    }

    private fun spawn(npc: NPC): Boolean {
        val index = index() ?: return false
        indexArray[index] = npc
        npc.index = index
        npc.levels.link(npc, NPCLevels(npc.def))
        npc.levels.clear(Skill.Constitution)
        npc.levels.clear(Skill.Attack)
        npc.levels.clear(Skill.Strength)
        npc.levels.clear(Skill.Defence)
        npc.levels.clear(Skill.Ranged)
        npc.levels.clear(Skill.Magic)
        npc["spawn_tile"] = npc.tile
        if (npc.mode == EmptyMode && Wander.wanders(npc)) {
            npc.mode = Wander(npc, npc.tile)
        }
        npc.collision = collision.get(npc)
        map.add(npc.tile.regionLevel, npc)
        val respawnDelay = npc.def.getOrNull<Int>("respawn_delay")
        if (respawnDelay != null && respawnDelay >= 0) {
            npc["respawn_tile"] = npc.tile
            npc["respawn_delay"] = respawnDelay
            npc["respawn_direction"] = npc.direction
        }
        Spawn.spawn(npc)
        return true
    }

    fun clear(region: RegionLevel) {
        for (index in map[region] ?: return) {
            if (removeIndex < removeQueue.size) {
                removeQueue[removeIndex++] = index
            }
        }
    }

    fun clear() {
        for (npc in this) {
            npc.emit(Despawn)
            npc.softTimers.stopAll()
        }
        indexArray.fill(null)
        indexer = 1
        size = 0
    }

    override fun iterator(): Iterator<NPC> = object : Iterator<NPC> {
        private var nextIndex = 1

        init {
            nextIndex()
        }

        override fun hasNext(): Boolean = nextIndex < indexArray.size

        override fun next(): NPC {
            if (!hasNext()) {
                throw NoSuchElementException("No more elements in the NPC array")
            }

            val current = indexArray[nextIndex++]!!
            nextIndex()
            return current
        }

        private fun nextIndex() {
            while (nextIndex < indexArray.size && indexArray[nextIndex] == null) {
                nextIndex++
            }
        }
    }
}
