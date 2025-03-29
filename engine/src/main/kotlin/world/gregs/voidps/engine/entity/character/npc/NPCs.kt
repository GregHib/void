package world.gregs.voidps.engine.entity.character.npc

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.MAX_NPCS
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.CharacterMap
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.RegionLevel
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import kotlin.math.log

data class NPCs(
    private val definitions: NPCDefinitions,
    private val collisions: Collisions,
    private val collision: CollisionStrategyProvider,
    private val areaDefinitions: AreaDefinitions
) : CharacterList<NPC>(), Runnable {
    override val indexArray: Array<NPC?> = arrayOfNulls(MAX_NPCS)
    private val logger = InlineLogger()
    private val map: CharacterMap = CharacterMap()
    private val spawnQueue: Array<NPC?> = arrayOfNulls(MAX_NPCS / 4)
    private val despawnQueue: Array<NPC?> = arrayOfNulls(MAX_NPCS / 4)
    private var spawnIndex = 0
    private var despawnIndex = 0

    override fun run() {
        for (i in 0 until despawnIndex) {
            val npc = despawnQueue[i] ?: continue
            if (!despawn(npc)) {
                logger.warn { "Failed to despawn $npc" }
            }
            removeIndex(npc)
            super.remove(npc)
            npc.index = -1
            despawnQueue[i] = null
        }
        despawnIndex = 0
        for (i in 0 until spawnIndex) {
            val npc = spawnQueue[i] ?: continue
            if (!spawn(npc)) {
                logger.warn { "Failed to spawn $npc" }
            }
            spawnQueue[i] = null
        }
        spawnIndex = 0
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
        }
    }

    fun hide(npc: NPC) = removeIndex(npc)

    fun show(npc: NPC) = index(npc)

    override fun remove(element: NPC): Boolean {
        if (element.index == -1) {
            logger.warn { "Unable to remove npc ${element}." }
            return false
        }
        if (despawnIndex < despawnQueue.size) {
            despawnQueue[despawnIndex++] = element
            return true
        }
        return false
    }

    fun getDirect(region: RegionLevel): List<Int>? = this.map[region]

    fun add(id: String, tile: Tile, direction: Direction = Direction.NONE): NPC? {
        val def = definitions.get(id)
        if (def.id == -1) {
            logger.warn { "No npc found for name $id" }
            return null
        }
        val npc = NPC(id, tile, def)
        if (spawnIndex < spawnQueue.size) {
            spawnQueue[spawnIndex++] = npc
        }
        val dir = if (direction == Direction.NONE) Direction.all.random() else direction
        npc.face(dir)
        return npc
    }

    private fun despawn(npc: NPC): Boolean {
        map.remove(npc.tile.regionLevel, npc)
        return super.remove(npc)
    }

    private fun spawn(npc: NPC): Boolean {
        val index = index() ?: return false
        npc.index = index
        npc.levels.link(npc, NPCLevels(npc.def))
        npc.levels.clear(Skill.Constitution)
        npc.levels.clear(Skill.Attack)
        npc.levels.clear(Skill.Strength)
        npc.levels.clear(Skill.Defence)
        npc.levels.clear(Skill.Ranged)
        npc.levels.clear(Skill.Magic)
        npc["spawn_tile"] = npc.tile
        if (Wander.wanders(npc)) {
            npc.mode = Wander(npc, npc.tile)
        }
        npc.collision = collision.get(npc)
        add(npc)
        val respawnDelay = npc.def.getOrNull<Int>("respawn_delay")
        if (respawnDelay != null && respawnDelay > 0) {
            npc["respawn_tile"] = npc.tile
            npc["respawn_delay"] = respawnDelay
            npc["respawn_direction"] = npc.direction
        }
        return true
    }

    override fun clear() {
        for (npc in this) {
            npc.emit(Despawn)
            npc.softTimers.stopAll()
        }
        super.clear()
    }
}